package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MerchantEntity
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.domain.AppLanguage
import com.example.domain.DeliveryCategory
import com.example.domain.LocalizationManager
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandPageBackground
import com.example.ui.theme.BrandPrimaryBlue
import com.example.ui.theme.BrandSecondaryBlue
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandTextPrimary
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.BrandYellow
import com.example.ui.theme.BrandYellowContainer
import com.example.ui.theme.BrandYellowHover
import com.example.ui.theme.OnBrandYellow
import com.example.ui.theme.OnBrandYellowContainer
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.DeliveryViewModel

@Composable
fun CustomerPortal(viewModel: DeliveryViewModel) {
    val language by viewModel.language.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Explore, 1: Cart, 2: Tracking & Orders

    val cartState by viewModel.cartState.collectAsState()
    val trackingOrderId by viewModel.trackingOrderId.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Customer subtabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BrandPrimaryBlue
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(LocalizationManager.get("tab_explore", language), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(LocalizationManager.get("tab_cart", language), fontWeight = FontWeight.Bold)
                        if (cartState.itemCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(BrandYellow)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = cartState.itemCount.toString(),
                                    color = OnBrandYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(LocalizationManager.get("tab_orders", language), fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> CustomerExploreTab(
                viewModel = viewModel,
                onOpenCart = { selectedTab = 1 }
            )
            1 -> CustomerCartTab(
                viewModel = viewModel,
                onOrderPlaced = { selectedTab = 2 }
            )
            2 -> CustomerOrdersAndTrackingTab(viewModel = viewModel)
        }
    }
}

@Composable
fun CustomerExploreTab(
    viewModel: DeliveryViewModel,
    onOpenCart: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val merchants by viewModel.filteredMerchants.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cartState by viewModel.cartState.collectAsState()

    var activeMerchantForMenu by remember { mutableStateOf<MerchantEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Announcement Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandPrimaryBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = null,
                                tint = BrandYellow,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LocalizationManager.get("tagline", language),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC)
                                "توصيل سريع وموثوق في 58 ولاية • كود ترحيبي: MARHABA10 (خصم 300 د.ج)"
                            else
                                "Livraison express dans 58 wilayas • Code de bienvenue: MARHABA10 (-300 DZD)",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text(LocalizationManager.get("search_hint", language), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = BrandSecondaryBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input")
                )
            }

            // Category Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        CategoryChip(
                            label = LocalizationManager.get("cat_all", language),
                            isSelected = selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) }
                        )
                    }
                    item {
                        CategoryChip(
                            label = LocalizationManager.get("cat_food", language),
                            isSelected = selectedCategory == DeliveryCategory.FOOD,
                            onClick = { viewModel.selectCategory(DeliveryCategory.FOOD) }
                        )
                    }
                    item {
                        CategoryChip(
                            label = LocalizationManager.get("cat_pharmacy", language),
                            isSelected = selectedCategory == DeliveryCategory.PHARMACY,
                            onClick = { viewModel.selectCategory(DeliveryCategory.PHARMACY) }
                        )
                    }
                    item {
                        CategoryChip(
                            label = LocalizationManager.get("cat_grocery", language),
                            isSelected = selectedCategory == DeliveryCategory.GROCERY,
                            onClick = { viewModel.selectCategory(DeliveryCategory.GROCERY) }
                        )
                    }
                    item {
                        CategoryChip(
                            label = LocalizationManager.get("cat_parcel", language),
                            isSelected = selectedCategory == DeliveryCategory.PARCEL,
                            onClick = { viewModel.selectCategory(DeliveryCategory.PARCEL) }
                        )
                    }
                }
            }

            // Merchant Cards
            items(merchants) { merchant ->
                MerchantCard(
                    merchant = merchant,
                    language = language,
                    onClick = {
                        viewModel.setMerchantId(merchant.id)
                        activeMerchantForMenu = merchant
                    }
                )
            }
        }

        // Floating Cart Summary Bar if items in cart
        if (cartState.itemCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clickable { onOpenCart() }
                    .testTag("floating_cart_bar"),
                shape = RoundedCornerShape(16.dp),
                color = BrandPrimaryBlue,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(BrandYellow)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${cartState.itemCount}",
                                color = OnBrandYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = LocalizationManager.get("tab_cart", language),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = LocalizationManager.formatDzd(cartState.total, language),
                        color = BrandYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // Merchant Menu Dialog
    activeMerchantForMenu?.let { merchant ->
        MerchantMenuDialog(
            merchant = merchant,
            viewModel = viewModel,
            language = language,
            onDismiss = { activeMerchantForMenu = null },
            onGoToCart = {
                activeMerchantForMenu = null
                onOpenCart()
            }
        )
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) BrandPrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun MerchantCard(
    merchant: MerchantEntity,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val name = if (language == AppLanguage.ARABIC) merchant.nameAr else merchant.nameFr
    val tag = if (language == AppLanguage.ARABIC) merchant.tagAr else merchant.tagFr

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("merchant_card_${merchant.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tag,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Rating pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandYellowContainer)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = OnBrandYellowContainer,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${merchant.rating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = OnBrandYellowContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // Footer info: Prep time, Delivery fee, Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Prep Time",
                        tint = BrandSecondaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${merchant.prepTimeMinutes} min",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = "Delivery Fee",
                        tint = BrandSecondaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LocalizationManager.formatDzd(merchant.deliveryFeeDzd, language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandPrimaryBlue
                    )
                }

                Text(
                    text = merchant.wilayaName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MerchantMenuDialog(
    merchant: MerchantEntity,
    viewModel: DeliveryViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onGoToCart: () -> Unit
) {
    val allProducts by viewModel.getProductsForMerchant(merchant.id).collectAsState(initial = emptyList())
    val cartState by viewModel.cartState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (language == AppLanguage.ARABIC) merchant.nameAr else merchant.nameFr,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = if (language == AppLanguage.ARABIC) merchant.address else merchant.address,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allProducts) { product ->
                    val prodName = if (language == AppLanguage.ARABIC) product.nameAr else product.nameFr
                    val prodDesc = if (language == AppLanguage.ARABIC) product.descAr else product.descFr
                    val quantityInCart = cartState.items[product] ?: 0

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = prodName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = prodDesc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = LocalizationManager.formatDzd(product.priceDzd, language),
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimaryBlue,
                                    fontSize = 13.sp
                                )
                            }

                            // Add/remove quantity controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (quantityInCart > 0) {
                                    IconButton(
                                        onClick = { viewModel.removeFromCart(product) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = StatusError)
                                    }
                                    Text(
                                        text = "$quantityInCart",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.addToCart(merchant, product) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(BrandPrimaryBlue)
                                        .testTag("add_to_cart_${product.id}")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onGoToCart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandYellow,
                    contentColor = OnBrandYellow
                )
            ) {
                Text(
                    text = if (language == AppLanguage.ARABIC) "متابعة إلى السلة" else "Voir mon panier (${cartState.itemCount})",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (language == AppLanguage.ARABIC) "إغلاق" else "Fermer")
            }
        }
    )
}

@Composable
fun CustomerCartTab(
    viewModel: DeliveryViewModel,
    onOrderPlaced: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val cartState by viewModel.cartState.collectAsState()
    var couponInput by remember { mutableStateOf("") }

    var customerName by remember { mutableStateOf("Amina Cherif") }
    var customerPhone by remember { mutableStateOf("+213 555 88 99 00") }
    var customerAddress by remember { mutableStateOf("Résidence Sahraoui, Bab Ezzouar, Alger") }
    var deliveryNotes by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH_ON_DELIVERY) }

    if (cartState.items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = BrandSecondaryBlue.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = LocalizationManager.get("cart_empty", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = LocalizationManager.get("cart_empty_sub", language),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Merchant header
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = cartState.merchant?.let { if (language == AppLanguage.ARABIC) it.nameAr else it.nameFr } ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = cartState.merchant?.wilayaName ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { viewModel.clearCart() }) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "تفريغ" else "Vider",
                            color = StatusError
                        )
                    }
                }
            }
        }

        // Cart Items
        items(cartState.items.entries.toList()) { (product, quantity) ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) product.nameAr else product.nameFr,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${LocalizationManager.formatDzd(product.priceDzd, language)} × $quantity",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.removeFromCart(product) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = StatusError)
                        }
                        Text(
                            text = "$quantity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = { cartState.merchant?.let { viewModel.addToCart(it, product) } },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandPrimaryBlue)
                        }
                    }
                }
            }
        }

        // Coupon Code Input
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it },
                            placeholder = { Text(LocalizationManager.get("coupon_hint", language), fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.applyCoupon(couponInput) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandSecondaryBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Text(LocalizationManager.get("apply_coupon", language), fontSize = 12.sp)
                        }
                    }
                    cartState.coupon?.let { coupon ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ ${coupon.code}: -${coupon.discountDzd.toInt()} DZD",
                            color = StatusSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    cartState.couponError?.let { err ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = err, color = StatusError, fontSize = 11.sp)
                    }
                }
            }
        }

        // Delivery Address & Client Info
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = LocalizationManager.get("delivery_address", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "الاسم واللقب" else "Nom complet") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "رقم الهاتف" else "Téléphone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "العنوان التفصيلي" else "Adresse précise") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deliveryNotes,
                        onValueChange = { deliveryNotes = it },
                        label = { Text(LocalizationManager.get("delivery_notes", language)) },
                        placeholder = { Text(if (language == AppLanguage.ARABIC) "مثال: الطابق الثاني، اتصل عند الباب" else "Ex: 2ème étage, sonner") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Payment Method Selector
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = LocalizationManager.get("payment_method", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    PaymentOptionRow(
                        title = LocalizationManager.get("pay_cod", language),
                        sub = if (language == AppLanguage.ARABIC) "ادفع نقداً عند استلام طلبيتك من السائق" else "Réglez en espèces à la livraison",
                        selected = selectedPayment == PaymentMethod.CASH_ON_DELIVERY,
                        onSelect = { selectedPayment = PaymentMethod.CASH_ON_DELIVERY }
                    )

                    PaymentOptionRow(
                        title = LocalizationManager.get("pay_edahabia", language),
                        sub = if (language == AppLanguage.ARABIC) "دفع إلكتروني فوري عبر بريد الجزائر" else "Paiement en ligne sécurisé BaridiMob",
                        selected = selectedPayment == PaymentMethod.EDAHABIA_BARIDIMOB,
                        onSelect = { selectedPayment = PaymentMethod.EDAHABIA_BARIDIMOB }
                    )

                    PaymentOptionRow(
                        title = LocalizationManager.get("pay_cib", language),
                        sub = if (language == AppLanguage.ARABIC) "دفع آمن بالبطاقة البنكية الوطنية CIB" else "Paiement par carte interbancaire",
                        selected = selectedPayment == PaymentMethod.CIB_CARD,
                        onSelect = { selectedPayment = PaymentMethod.CIB_CARD }
                    )
                }
            }
        }

        // Financial Totals Summary
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = LocalizationManager.get("subtotal", language), fontSize = 13.sp)
                        Text(text = LocalizationManager.formatDzd(cartState.subtotal, language), fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = LocalizationManager.get("delivery_fee", language), fontSize = 13.sp)
                        Text(text = LocalizationManager.formatDzd(cartState.deliveryFee, language), fontSize = 13.sp)
                    }
                    if (cartState.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = LocalizationManager.get("discount", language), color = StatusError, fontSize = 13.sp)
                            Text(text = "-${LocalizationManager.formatDzd(cartState.discount, language)}", color = StatusError, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = LocalizationManager.get("total", language), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = LocalizationManager.formatDzd(cartState.total, language),
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryBlue,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // Submit Button (Primary Call to Action in Brand Yellow)
        item {
            Button(
                onClick = {
                    viewModel.submitOrder(
                        customerPhone = customerPhone,
                        customerName = customerName,
                        customerAddress = customerAddress,
                        paymentMethod = selectedPayment,
                        notes = deliveryNotes
                    )
                    onOrderPlaced()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandYellow,
                    contentColor = OnBrandYellow
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_order_button")
            ) {
                Text(
                    text = "${LocalizationManager.get("checkout_btn", language)} • ${LocalizationManager.formatDzd(cartState.total, language)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PaymentOptionRow(
    title: String,
    sub: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = sub, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CustomerOrdersAndTrackingTab(viewModel: DeliveryViewModel) {
    val language by viewModel.language.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val trackingOrderId by viewModel.trackingOrderId.collectAsState()

    val activeTrackingOrder = orders.find { it.id == trackingOrderId } ?: orders.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Tracking Card
        if (activeTrackingOrder != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = LocalizationManager.get("tracking_title", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Réf: ${activeTrackingOrder.id}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                            OrderStatusBadge(status = activeTrackingOrder.status, language = language)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive animated map
                        InteractiveMapCanvas(
                            modifier = Modifier.fillMaxWidth(),
                            status = activeTrackingOrder.status,
                            merchantName = activeTrackingOrder.merchantName,
                            customerAddress = activeTrackingOrder.customerAddress,
                            driverName = activeTrackingOrder.driverName
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // OTP Proof-of-Delivery Code Box (CRITICAL FEATURE)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandYellowContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandYellow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = LocalizationManager.get("otp_code", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = OnBrandYellowContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeTrackingOrder.otpCode,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp,
                                    letterSpacing = 6.sp,
                                    color = BrandPrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = LocalizationManager.get("otp_instruction", language),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Driver Contact Card if assigned
                        if (activeTrackingOrder.driverName != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(BrandPrimaryBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = activeTrackingOrder.driverName ?: "",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = activeTrackingOrder.driverVehicle ?: "Scooter",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { /* Simulated secure dialer */ },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(LocalizationManager.get("call_driver", language), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // All Orders History Header
        item {
            Text(
                text = LocalizationManager.get("tab_orders", language),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // List of all orders
        items(orders) { order ->
            val isCurrentTracking = order.id == activeTrackingOrder?.id
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentTracking) BrandBlueContainer else MaterialTheme.colorScheme.surface
                ),
                border = if (isCurrentTracking) androidx.compose.foundation.BorderStroke(1.dp, BrandSecondaryBlue) else null,
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTrackingOrder(order.id) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "№ ${order.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        OrderStatusBadge(status = order.status, language = language)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = order.merchantName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LocalizationManager.formatDzd(order.totalDzd, language),
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryBlue,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (language == AppLanguage.ARABIC) "عرض التتبع التفصيلي ◄" else "Voir suivi ►",
                            color = BrandSecondaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
