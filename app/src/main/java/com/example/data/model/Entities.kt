package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.DeliveryCategory
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod

@Entity(tableName = "merchants")
data class MerchantEntity(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameFr: String,
    val category: DeliveryCategory,
    val wilayaCode: String,
    val wilayaName: String,
    val address: String,
    val rating: Double,
    val prepTimeMinutes: Int,
    val deliveryFeeDzd: Double,
    val minOrderDzd: Double,
    val isOpen: Boolean = true,
    val tagAr: String = "",
    val tagFr: String = ""
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val merchantId: String,
    val nameAr: String,
    val nameFr: String,
    val descAr: String,
    val descFr: String,
    val priceDzd: Double,
    val categoryName: String,
    val isAvailable: Boolean = true,
    val isPopular: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String, // e.g., "DZ-7482"
    val customerPhone: String,
    val customerName: String,
    val customerAddress: String,
    val wilayaCode: String,
    val merchantId: String,
    val merchantName: String,
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverVehicle: String? = null,
    val status: OrderStatus,
    val subtotalDzd: Double,
    val deliveryFeeDzd: Double,
    val discountDzd: Double,
    val totalDzd: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: String = "PENDING", // PENDING, PAID
    val notes: String = "",
    val otpCode: String, // 4 digit proof-of-delivery OTP
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val productId: String,
    val productNameAr: String,
    val productNameFr: String,
    val unitPriceDzd: Double,
    val quantity: Int
)

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val vehicleType: String,
    val wilayaCode: String,
    val isOnline: Boolean,
    val rating: Double,
    val totalDeliveries: Int,
    val todayEarningsDzd: Double,
    val activeOrderId: String? = null
)

@Entity(tableName = "wilayas")
data class WilayaEntity(
    @PrimaryKey val code: String,
    val nameAr: String,
    val nameFr: String,
    val baseDeliveryFeeDzd: Double,
    val isActive: Boolean = true
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val code: String,
    val discountDzd: Double,
    val minOrderDzd: Double,
    val isActive: Boolean = true
)
