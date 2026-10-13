package com.example.domain

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    ASSIGNED,
    PICKED_UP,
    DELIVERING,
    DELIVERED,
    CANCELLED;

    fun displayName(lang: AppLanguage): String = when (this) {
        PENDING -> LocalizationManager.get("status_pending", lang)
        CONFIRMED -> LocalizationManager.get("status_confirmed", lang)
        PREPARING -> LocalizationManager.get("status_preparing", lang)
        READY_FOR_PICKUP -> LocalizationManager.get("status_ready_for_pickup", lang)
        ASSIGNED -> LocalizationManager.get("status_assigned", lang)
        PICKED_UP -> LocalizationManager.get("status_picked_up", lang)
        DELIVERING -> LocalizationManager.get("status_delivering", lang)
        DELIVERED -> LocalizationManager.get("status_delivered", lang)
        CANCELLED -> LocalizationManager.get("status_cancelled", lang)
    }

    fun isTerminal(): Boolean = this == DELIVERED || this == CANCELLED

    fun nextLogicalStatus(): OrderStatus? = when (this) {
        PENDING -> CONFIRMED
        CONFIRMED -> PREPARING
        PREPARING -> READY_FOR_PICKUP
        READY_FOR_PICKUP -> ASSIGNED
        ASSIGNED -> PICKED_UP
        PICKED_UP -> DELIVERING
        DELIVERING -> DELIVERED
        DELIVERED, CANCELLED -> null
    }

    fun allowedTransitions(): Set<OrderStatus> = when (this) {
        PENDING -> setOf(CONFIRMED, CANCELLED)
        CONFIRMED -> setOf(PREPARING, CANCELLED)
        PREPARING -> setOf(READY_FOR_PICKUP, CANCELLED)
        READY_FOR_PICKUP -> setOf(ASSIGNED, CANCELLED)
        ASSIGNED -> setOf(PICKED_UP, CANCELLED)
        PICKED_UP -> setOf(DELIVERING)
        DELIVERING -> setOf(DELIVERED)
        DELIVERED, CANCELLED -> emptySet()
    }
}

enum class DeliveryCategory {
    FOOD,
    PHARMACY,
    GROCERY,
    PARCEL,
    CUSTOM_ERRAND
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    EDAHABIA_BARIDIMOB,
    CIB_CARD
}

enum class UserRole {
    CUSTOMER,
    DRIVER,
    MERCHANT,
    ADMIN
}
