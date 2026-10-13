package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseMerchantDto(
    val id: String,
    @Json(name = "name_ar") val nameAr: String,
    @Json(name = "name_fr") val nameFr: String,
    val address: String,
    val rating: Double?,
    @Json(name = "prep_time_minutes") val prepTimeMinutes: Int?,
    @Json(name = "delivery_fee") val deliveryFee: Double?,
    @Json(name = "min_order_amount") val minOrderAmount: Double?,
    @Json(name = "is_open") val isOpen: Boolean?,
    @Json(name = "tag_ar") val tagAr: String?,
    @Json(name = "tag_fr") val tagFr: String?
)

@JsonClass(generateAdapter = true)
data class SupabaseProductDto(
    val id: String,
    @Json(name = "merchant_id") val merchantId: String,
    @Json(name = "name_ar") val nameAr: String,
    @Json(name = "name_fr") val nameFr: String,
    @Json(name = "desc_ar") val descAr: String?,
    @Json(name = "desc_fr") val descFr: String?,
    val price: Double,
    @Json(name = "category_name") val categoryName: String?,
    @Json(name = "is_available") val isAvailable: Boolean?,
    @Json(name = "is_popular") val isPopular: Boolean?
)

@JsonClass(generateAdapter = true)
data class SupabaseOrderDto(
    val id: String,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_address") val customerAddress: String,
    @Json(name = "wilaya_code") val wilayaCode: String,
    @Json(name = "merchant_id") val merchantId: String,
    @Json(name = "merchant_name") val merchantName: String,
    @Json(name = "driver_id") val driverId: String?,
    @Json(name = "driver_name") val driverName: String?,
    @Json(name = "driver_phone") val driverPhone: String?,
    @Json(name = "driver_vehicle") val driverVehicle: String?,
    val status: String,
    val subtotal: Double,
    @Json(name = "delivery_fee") val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "payment_status") val paymentStatus: String,
    val notes: String?,
    @Json(name = "otp_code") val otpCode: String
)

@JsonClass(generateAdapter = true)
data class CreateOrderRpcRequest(
    val merchantId: String,
    val items: List<CartItemDto>,
    val deliveryAddress: String,
    val wilayaCode: String,
    val customerPhone: String,
    val customerName: String,
    val couponCode: String?,
    val paymentMethod: String,
    val notes: String?
)

@JsonClass(generateAdapter = true)
data class CartItemDto(
    val productId: String,
    val quantity: Int
)

@JsonClass(generateAdapter = true)
data class RpcStatusResponse(
    val success: Boolean,
    val error: String?,
    @Json(name = "order_id") val orderId: String?,
    val status: String?
)

@JsonClass(generateAdapter = true)
data class TransitionStatusRequest(
    @Json(name = "p_order_id") val orderId: String,
    @Json(name = "p_new_status") val newStatus: String,
    @Json(name = "p_reason") val reason: String?
)

@JsonClass(generateAdapter = true)
data class AssignDriverRequest(
    @Json(name = "p_order_id") val orderId: String,
    @Json(name = "p_driver_id") val driverId: String
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    @Json(name = "p_order_id") val orderId: String,
    @Json(name = "p_entered_otp") val enteredOtp: String,
    @Json(name = "p_driver_id") val driverId: String
)
