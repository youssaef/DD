package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CouponEntity
import com.example.data.model.DriverEntity
import com.example.data.model.MerchantEntity
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WilayaEntity
import com.example.data.repository.DzDeliveryRepository
import com.example.domain.AppLanguage
import com.example.domain.DeliveryCategory
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod
import com.example.domain.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartState(
    val items: Map<ProductEntity, Int> = emptyMap(),
    val merchant: MerchantEntity? = null,
    val coupon: CouponEntity? = null,
    val couponError: String? = null
) {
    val subtotal: Double
        get() = items.entries.sumOf { it.key.priceDzd * it.value }

    val deliveryFee: Double
        get() = merchant?.deliveryFeeDzd ?: 400.0

    val discount: Double
        get() = coupon?.discountDzd ?: 0.0

    val total: Double
        get() = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)

    val itemCount: Int
        get() = items.values.sum()
}

class DeliveryViewModel(private val repository: DzDeliveryRepository) : ViewModel() {

    // Language & RTL/LTR Layout
    private val _language = MutableStateFlow(AppLanguage.FRENCH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.ARABIC) AppLanguage.FRENCH else AppLanguage.ARABIC
    }

    // Role switcher
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    // Customer filters
    private val _selectedCategory = MutableStateFlow<DeliveryCategory?>(null)
    val selectedCategory: StateFlow<DeliveryCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedWilayaCode = MutableStateFlow("16") // Default Alger
    val selectedWilayaCode: StateFlow<String> = _selectedWilayaCode.asStateFlow()

    fun selectCategory(category: DeliveryCategory?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectWilaya(code: String) {
        _selectedWilayaCode.value = code
    }

    // Repository flows
    val allWilayas: StateFlow<List<WilayaEntity>> = repository.allWilayas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMerchants: StateFlow<List<MerchantEntity>> = repository.allMerchants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMerchants: StateFlow<List<MerchantEntity>> = combine(
        repository.allMerchants,
        _selectedCategory,
        _searchQuery,
        _selectedWilayaCode
    ) { merchants, category, query, wilaya ->
        merchants.filter { merchant ->
            val matchesCategory = category == null || merchant.category == category
            val matchesQuery = query.isBlank() ||
                    merchant.nameFr.contains(query, ignoreCase = true) ||
                    merchant.nameAr.contains(query, ignoreCase = true) ||
                    merchant.tagFr.contains(query, ignoreCase = true) ||
                    merchant.tagAr.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDrivers: StateFlow<List<DriverEntity>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Management
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    fun addToCart(merchant: MerchantEntity, product: ProductEntity) {
        val current = _cartState.value
        // If items are from another merchant, reset cart to new merchant
        val activeMerchant = if (current.merchant?.id == merchant.id) current.merchant else merchant
        val updatedMap = if (current.merchant?.id == merchant.id) current.items.toMutableMap() else mutableMapOf()
        val currentQty = updatedMap[product] ?: 0
        updatedMap[product] = currentQty + 1

        _cartState.value = current.copy(
            items = updatedMap,
            merchant = activeMerchant,
            couponError = null
        )
    }

    fun removeFromCart(product: ProductEntity) {
        val current = _cartState.value
        val updatedMap = current.items.toMutableMap()
        val currentQty = updatedMap[product] ?: 0
        if (currentQty <= 1) {
            updatedMap.remove(product)
        } else {
            updatedMap[product] = currentQty - 1
        }
        _cartState.value = current.copy(
            items = updatedMap,
            merchant = if (updatedMap.isEmpty()) null else current.merchant
        )
    }

    fun clearCart() {
        _cartState.value = CartState()
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch {
            val subtotal = _cartState.value.subtotal
            val coupon = repository.validateCoupon(code, subtotal)
            if (coupon != null) {
                _cartState.value = _cartState.value.copy(coupon = coupon, couponError = null)
            } else {
                _cartState.value = _cartState.value.copy(
                    coupon = null,
                    couponError = if (_language.value == AppLanguage.ARABIC)
                        "رمز القسيمة غير صالح أو لم يصل للحد الأدنى للطلب"
                    else
                        "Code promo invalide ou montant minimum non atteint"
                )
            }
        }
    }

    // Customer Active Tracking Order
    private val _trackingOrderId = MutableStateFlow<String?>("DZ-8021") // Initial demo order
    val trackingOrderId: StateFlow<String?> = _trackingOrderId.asStateFlow()

    fun selectTrackingOrder(orderId: String) {
        _trackingOrderId.value = orderId
    }

    // Checkout
    private val _checkoutSuccessOrder = MutableStateFlow<OrderEntity?>(null)
    val checkoutSuccessOrder: StateFlow<OrderEntity?> = _checkoutSuccessOrder.asStateFlow()

    fun submitOrder(
        customerPhone: String,
        customerName: String,
        customerAddress: String,
        paymentMethod: PaymentMethod,
        notes: String
    ) {
        val currentCart = _cartState.value
        val merchant = currentCart.merchant ?: return
        if (currentCart.items.isEmpty()) return

        viewModelScope.launch {
            val order = repository.createOrder(
                customerPhone = customerPhone.ifBlank { "+213 555 88 99 00" },
                customerName = customerName.ifBlank { "Client DZ" },
                customerAddress = customerAddress.ifBlank { "Bab Ezzouar, Alger" },
                wilayaCode = _selectedWilayaCode.value,
                merchantId = merchant.id,
                merchantName = merchant.nameFr,
                subtotalDzd = currentCart.subtotal,
                deliveryFeeDzd = currentCart.deliveryFee,
                discountDzd = currentCart.discount,
                paymentMethod = paymentMethod,
                notes = notes,
                cartItems = currentCart.items.map { Pair(it.key, it.value) }
            )
            clearCart()
            _trackingOrderId.value = order.id
            _checkoutSuccessOrder.value = order
        }
    }

    fun dismissCheckoutSuccess() {
        _checkoutSuccessOrder.value = null
    }

    // Merchant Space
    private val _selectedMerchantId = MutableStateFlow("m_bardo")
    val selectedMerchantId: StateFlow<String> = _selectedMerchantId.asStateFlow()

    fun setMerchantId(id: String) {
        _selectedMerchantId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMerchantProducts: StateFlow<List<ProductEntity>> = _selectedMerchantId
        .flatMapLatest { id -> repository.getProductsForMerchant(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getProductsForMerchant(id: String): Flow<List<ProductEntity>> =
        repository.getProductsForMerchant(id)

    fun merchantAcceptOrder(orderId: String) {
        viewModelScope.launch {
            repository.merchantAcceptOrder(orderId)
        }
    }

    fun merchantMarkReady(orderId: String) {
        viewModelScope.launch {
            repository.merchantMarkReady(orderId)
        }
    }

    fun toggleProductAvailability(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product.copy(isAvailable = !product.isAvailable))
        }
    }

    // Driver Space
    private val _currentDriverId = MutableStateFlow("d_karim")
    val currentDriverId: StateFlow<String> = _currentDriverId.asStateFlow()

    fun setDriverId(id: String) {
        _currentDriverId.value = id
    }

    val availableOrdersForDrivers: StateFlow<List<OrderEntity>> = repository.availableOrdersForDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _otpVerificationError = MutableStateFlow<String?>(null)
    val otpVerificationError: StateFlow<String?> = _otpVerificationError.asStateFlow()

    fun driverToggleOnline(driverId: String, currentOnline: Boolean) {
        viewModelScope.launch {
            repository.setDriverOnline(driverId, !currentOnline)
        }
    }

    fun driverAcceptOrder(orderId: String, driverId: String) {
        viewModelScope.launch {
            repository.driverAcceptOrder(orderId, driverId)
        }
    }

    fun driverConfirmPickup(orderId: String) {
        viewModelScope.launch {
            repository.driverConfirmPickup(orderId)
        }
    }

    fun driverConfirmDeliveryWithOtp(orderId: String, enteredOtp: String) {
        viewModelScope.launch {
            val success = repository.driverValidateDeliveryOtp(orderId, enteredOtp)
            if (success) {
                _otpVerificationError.value = null
            } else {
                _otpVerificationError.value = if (_language.value == AppLanguage.ARABIC)
                    "رمز OTP غير مطابق! تأكد من الزبون"
                else
                    "Code OTP incorrect ! Vérifiez auprès du client"
            }
        }
    }

    fun clearOtpError() {
        _otpVerificationError.value = null
    }

    // Admin Dispatch
    fun adminAssignDriver(orderId: String, driverId: String) {
        viewModelScope.launch {
            repository.adminAssignDriver(orderId, driverId)
        }
    }

    fun adminCancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId, reason)
        }
    }
}

class DeliveryViewModelFactory(private val repository: DzDeliveryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
