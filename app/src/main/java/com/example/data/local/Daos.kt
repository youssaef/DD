package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CouponEntity
import com.example.data.model.DriverEntity
import com.example.data.model.MerchantEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WilayaEntity
import com.example.domain.DeliveryCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantDao {
    @Query("SELECT * FROM merchants ORDER BY rating DESC")
    fun getAllMerchants(): Flow<List<MerchantEntity>>

    @Query("SELECT * FROM merchants WHERE category = :category ORDER BY rating DESC")
    fun getMerchantsByCategory(category: DeliveryCategory): Flow<List<MerchantEntity>>

    @Query("SELECT * FROM merchants WHERE id = :id LIMIT 1")
    fun getMerchantById(id: String): Flow<MerchantEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchants(merchants: List<MerchantEntity>)

    @Update
    suspend fun updateMerchant(merchant: MerchantEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE merchantId = :merchantId ORDER BY isPopular DESC, nameFr ASC")
    fun getProductsByMerchant(merchantId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAtMillis DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerPhone = :phone ORDER BY createdAtMillis DESC")
    fun getOrdersByCustomer(phone: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE merchantId = :merchantId ORDER BY createdAtMillis DESC")
    fun getOrdersByMerchant(merchantId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE driverId = :driverId ORDER BY createdAtMillis DESC")
    fun getOrdersByDriver(driverId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'READY_FOR_PICKUP' AND driverId IS NULL ORDER BY createdAtMillis ASC")
    fun getAvailableOrdersForDrivers(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderByIdDirect(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsDirect(orderId: String): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)
}

@Dao
interface DriverDao {
    @Query("SELECT * FROM drivers")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    fun getDriverById(id: String): Flow<DriverEntity?>

    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    suspend fun getDriverByIdDirect(id: String): DriverEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverEntity>)

    @Update
    suspend fun updateDriver(driver: DriverEntity)
}

@Dao
interface WilayaDao {
    @Query("SELECT * FROM wilayas WHERE isActive = 1 ORDER BY code ASC")
    fun getAllWilayas(): Flow<List<WilayaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWilayas(wilayas: List<WilayaEntity>)

    @Update
    suspend fun updateWilaya(wilaya: WilayaEntity)
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons WHERE isActive = 1")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCoupon(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)
}
