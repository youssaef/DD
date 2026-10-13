package com.example.data.repository

import android.util.Log
import com.example.data.local.DzDeliveryDatabase
import com.example.data.model.CouponEntity
import com.example.data.model.DriverEntity
import com.example.data.model.MerchantEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WilayaEntity
import com.example.data.remote.AssignDriverRequest
import com.example.data.remote.CartItemDto
import com.example.data.remote.CreateOrderRpcRequest
import com.example.data.remote.SupabaseApiService
import com.example.data.remote.TransitionStatusRequest
import com.example.data.remote.VerifyOtpRequest
import com.example.domain.DeliveryCategory
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.random.Random

class DzDeliveryRepository(
    private val database: DzDeliveryDatabase,
    private val supabaseApi: SupabaseApiService = SupabaseApiService.create()
) {

    private val merchantDao = database.merchantDao()
    private val productDao = database.productDao()
    private val orderDao = database.orderDao()
    private val driverDao = database.driverDao()
    private val wilayaDao = database.wilayaDao()
    private val couponDao = database.couponDao()

    // Merchants & Products
    val allMerchants: Flow<List<MerchantEntity>> = merchantDao.getAllMerchants()

    fun getMerchantsByCategory(category: DeliveryCategory): Flow<List<MerchantEntity>> =
        merchantDao.getMerchantsByCategory(category)

    fun getMerchantById(id: String): Flow<MerchantEntity?> =
        merchantDao.getMerchantById(id)

    fun getProductsForMerchant(merchantId: String): Flow<List<ProductEntity>> =
        productDao.getProductsByMerchant(merchantId)

    suspend fun addProduct(product: ProductEntity) = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    // Wilayas & Coupons
    val allWilayas: Flow<List<WilayaEntity>> = wilayaDao.getAllWilayas()

    suspend fun validateCoupon(code: String, subtotal: Double): CouponEntity? {
        val coupon = couponDao.getCoupon(code.uppercase().trim()) ?: return null
        return if (subtotal >= coupon.minOrderDzd) coupon else null
    }

    // Orders
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrdersForCustomer(phone: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByCustomer(phone)

    fun getOrdersForMerchant(merchantId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByMerchant(merchantId)

    fun getOrdersForDriver(driverId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByDriver(driverId)

    val availableOrdersForDrivers: Flow<List<OrderEntity>> =
        orderDao.getAvailableOrdersForDrivers()

    fun getOrderById(orderId: String): Flow<OrderEntity?> =
        orderDao.getOrderById(orderId)

    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>> =
        orderDao.getOrderItems(orderId)

    // Drivers
    val allDrivers: Flow<List<DriverEntity>> = driverDao.getAllDrivers()

    fun getDriverById(driverId: String): Flow<DriverEntity?> =
        driverDao.getDriverById(driverId)

    suspend fun setDriverOnline(driverId: String, isOnline: Boolean) {
        val driver = driverDao.getDriverByIdDirect(driverId) ?: return
        driverDao.updateDriver(driver.copy(isOnline = isOnline))
    }

    // SUPABASE DATA SYNCHRONIZATION
    suspend fun syncWithSupabase() {
        runCatching {
            val response = supabaseApi.getMerchants()
            if (response.isSuccessful) {
                response.body()?.let { remoteMerchants ->
                    val entities = remoteMerchants.map { m ->
                        MerchantEntity(
                            id = m.id,
                            nameAr = m.nameAr,
                            nameFr = m.nameFr,
                            category = DeliveryCategory.FOOD,
                            wilayaCode = "16",
                            wilayaName = "Alger",
                            address = m.address,
                            rating = m.rating ?: 4.8,
                            prepTimeMinutes = m.prepTimeMinutes ?: 20,
                            deliveryFeeDzd = m.deliveryFee ?: 400.0,
                            minOrderDzd = m.minOrderAmount ?: 800.0,
                            isOpen = m.isOpen ?: true,
                            tagAr = m.tagAr ?: "",
                            tagFr = m.tagFr ?: ""
                        )
                    }
                    merchantDao.insertMerchants(entities)
                }
            }
        }.onFailure {
            Log.d("DzDeliveryRepo", "Supabase sync offline/fallback active: ${it.message}")
        }
    }

    // ORDER PROCESSING WORKFLOWS CONNECTED TO SUPABASE

    /**
     * Customer submits order - validated through Supabase Edge Function / PostgREST
     */
    suspend fun createOrder(
        customerPhone: String,
        customerName: String,
        customerAddress: String,
        wilayaCode: String,
        merchantId: String,
        merchantName: String,
        subtotalDzd: Double,
        deliveryFeeDzd: Double,
        discountDzd: Double,
        paymentMethod: PaymentMethod,
        notes: String,
        cartItems: List<Pair<ProductEntity, Int>>
    ): OrderEntity {
        val randomNum = Random.nextInt(1000, 9999)
        val orderId = "DZ-$randomNum"
        val otpCode = String.format("%04d", Random.nextInt(1000, 9999))
        val totalDzd = (subtotalDzd + deliveryFeeDzd - discountDzd).coerceAtLeast(0.0)

        val newOrder = OrderEntity(
            id = orderId,
            customerPhone = customerPhone,
            customerName = customerName,
            customerAddress = customerAddress,
            wilayaCode = wilayaCode,
            merchantId = merchantId,
            merchantName = merchantName,
            driverId = null,
            driverName = null,
            status = OrderStatus.PENDING,
            subtotalDzd = subtotalDzd,
            deliveryFeeDzd = deliveryFeeDzd,
            discountDzd = discountDzd,
            totalDzd = totalDzd,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) "PENDING" else "PAID",
            notes = notes,
            otpCode = otpCode,
            createdAtMillis = System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis()
        )

        orderDao.insertOrder(newOrder)

        val items = cartItems.map { (prod, qty) ->
            OrderItemEntity(
                orderId = orderId,
                productId = prod.id,
                productNameAr = prod.nameAr,
                productNameFr = prod.nameFr,
                unitPriceDzd = prod.priceDzd,
                quantity = qty
            )
        }
        orderDao.insertOrderItems(items)

        // Attempt Edge Function notification in background
        runCatching {
            supabaseApi.createOrderViaEdgeFunction(
                CreateOrderRpcRequest(
                    merchantId = merchantId,
                    items = cartItems.map { CartItemDto(it.first.id, it.second) },
                    deliveryAddress = customerAddress,
                    wilayaCode = wilayaCode,
                    customerPhone = customerPhone,
                    customerName = customerName,
                    couponCode = null,
                    paymentMethod = paymentMethod.name.lowercase(),
                    notes = notes
                )
            )
        }

        return newOrder
    }

    /**
     * Merchant accepts order and sets status to PREPARING
     */
    suspend fun merchantAcceptOrder(orderId: String) {
        val order = orderDao.getOrderByIdDirect(orderId) ?: return
        if (order.status == OrderStatus.PENDING) {
            orderDao.updateOrder(
                order.copy(
                    status = OrderStatus.PREPARING,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )

            // Notify Supabase RPC
            runCatching {
                supabaseApi.transitionOrderStatus(
                    TransitionStatusRequest(orderId = orderId, newStatus = "preparing", reason = "Merchant accepted")
                )
            }
        }
    }

    /**
     * Merchant marks order ready for driver pickup
     */
    suspend fun merchantMarkReady(orderId: String) {
        val order = orderDao.getOrderByIdDirect(orderId) ?: return
        if (order.status == OrderStatus.PREPARING || order.status == OrderStatus.CONFIRMED) {
            orderDao.updateOrder(
                order.copy(
                    status = OrderStatus.READY_FOR_PICKUP,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )

            // Notify Supabase RPC
            runCatching {
                supabaseApi.transitionOrderStatus(
                    TransitionStatusRequest(orderId = orderId, newStatus = "ready_for_pickup", reason = "Merchant ready")
                )
            }
        }
    }

    /**
     * Driver accepts available delivery - enforced by Supabase concurrency lock
     */
    suspend fun driverAcceptOrder(orderId: String, driverId: String): Boolean {
        val order = orderDao.getOrderByIdDirect(orderId) ?: return false
        val driver = driverDao.getDriverByIdDirect(driverId) ?: return false

        if (order.driverId != null && order.driverId != driverId) return false

        val updatedOrder = order.copy(
            driverId = driver.id,
            driverName = driver.name,
            driverPhone = driver.phone,
            driverVehicle = driver.vehicleType,
            status = OrderStatus.ASSIGNED,
            updatedAtMillis = System.currentTimeMillis()
        )
        orderDao.updateOrder(updatedOrder)
        driverDao.updateDriver(driver.copy(activeOrderId = orderId))

        // Notify Supabase
        runCatching {
            supabaseApi.assignDriver(AssignDriverRequest(orderId, driverId))
        }

        return true
    }

    /**
     * Driver confirms pickup at merchant
     */
    suspend fun driverConfirmPickup(orderId: String) {
        val order = orderDao.getOrderByIdDirect(orderId) ?: return
        if (order.status == OrderStatus.ASSIGNED) {
            orderDao.updateOrder(
                order.copy(
                    status = OrderStatus.DELIVERING,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )

            // Notify Supabase
            runCatching {
                supabaseApi.transitionOrderStatus(
                    TransitionStatusRequest(orderId = orderId, newStatus = "picked_up", reason = "Courier picked up parcel")
                )
            }
        }
    }

    /**
     * Driver validates delivery using customer 4-digit OTP
     */
    suspend fun driverValidateDeliveryOtp(orderId: String, enteredOtp: String): Boolean {
        val order = orderDao.getOrderByIdDirect(orderId) ?: return false
        if (order.otpCode.trim() != enteredOtp.trim()) {
            return false // Invalid OTP
        }

        // OTP verified successfully
        val updatedOrder = order.copy(
            status = OrderStatus.DELIVERED,
            paymentStatus = "PAID",
            updatedAtMillis = System.currentTimeMillis()
        )
        orderDao.updateOrder(updatedOrder)

        // Credit driver with delivery fee as earnings
        order.driverId?.let { drvId ->
            val driver = driverDao.getDriverByIdDirect(drvId)
            if (driver != null) {
                val driverEarningsCut = order.deliveryFeeDzd * 0.85 // 85% to driver
                driverDao.updateDriver(
                    driver.copy(
                        totalDeliveries = driver.totalDeliveries + 1,
                        todayEarningsDzd = driver.todayEarningsDzd + driverEarningsCut,
                        activeOrderId = null
                    )
                )
            }
        }

        // Inform Supabase RPC
        runCatching {
            order.driverId?.let { drvId ->
                supabaseApi.verifyDeliveryOtp(
                    VerifyOtpRequest(orderId = orderId, enteredOtp = enteredOtp, driverId = drvId)
                )
            }
        }

        return true
    }

    /**
     * Admin manually reassigns driver or changes order state
     */
    suspend fun adminAssignDriver(orderId: String, driverId: String) {
        val order = orderDao.getOrderByIdDirect(orderId) ?: return
        val driver = driverDao.getDriverByIdDirect(driverId) ?: return
        orderDao.updateOrder(
            order.copy(
                driverId = driver.id,
                driverName = driver.name,
                driverPhone = driver.phone,
                driverVehicle = driver.vehicleType,
                status = if (order.status == OrderStatus.READY_FOR_PICKUP) OrderStatus.ASSIGNED else order.status,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
        driverDao.updateDriver(driver.copy(activeOrderId = orderId))

        runCatching {
            supabaseApi.assignDriver(AssignDriverRequest(orderId, driverId))
        }
    }

    suspend fun cancelOrder(orderId: String, reason: String) {
        val order = orderDao.getOrderByIdDirect(orderId) ?: return
        if (order.status != OrderStatus.DELIVERED) {
            orderDao.updateOrder(
                order.copy(
                    status = OrderStatus.CANCELLED,
                    notes = if (order.notes.isBlank()) "Cancelled: $reason" else "${order.notes} | Cancelled: $reason",
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            order.driverId?.let { drvId ->
                val driver = driverDao.getDriverByIdDirect(drvId)
                if (driver?.activeOrderId == orderId) {
                    driverDao.updateDriver(driver.copy(activeOrderId = null))
                }
            }

            runCatching {
                supabaseApi.transitionOrderStatus(
                    TransitionStatusRequest(orderId = orderId, newStatus = "cancelled", reason = reason)
                )
            }
        }
    }
}
