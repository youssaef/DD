package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.DeliveryCategory
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod

class Converters {
    @TypeConverter
    fun fromDeliveryCategory(value: DeliveryCategory?): String? = value?.name

    @TypeConverter
    fun toDeliveryCategory(value: String?): DeliveryCategory? =
        value?.let { runCatching { DeliveryCategory.valueOf(it) }.getOrDefault(DeliveryCategory.FOOD) }

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus?): String? = value?.name

    @TypeConverter
    fun toOrderStatus(value: String?): OrderStatus? =
        value?.let { runCatching { OrderStatus.valueOf(it) }.getOrDefault(OrderStatus.PENDING) }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod?): String? = value?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? =
        value?.let { runCatching { PaymentMethod.valueOf(it) }.getOrDefault(PaymentMethod.CASH_ON_DELIVERY) }
}
