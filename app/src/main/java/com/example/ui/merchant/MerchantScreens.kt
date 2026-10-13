package com.example.ui.merchant

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AppLanguage
import com.example.domain.LocalizationManager
import com.example.domain.OrderStatus
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandPrimaryBlue
import com.example.ui.theme.BrandSecondaryBlue
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandTextPrimary
import com.example.ui.theme.BrandYellow
import com.example.ui.theme.BrandYellowContainer
import com.example.ui.theme.OnBrandYellow
import com.example.ui.theme.OnBrandYellowContainer
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.DeliveryViewModel

@Composable
fun MerchantPortal(viewModel: DeliveryViewModel) {
    val language by viewModel.language.collectAsState()
    val merchants by viewModel.allMerchants.collectAsState()
    val selectedMerchantId by viewModel.selectedMerchantId.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val products by viewModel.currentMerchantProducts.collectAsState()

    val currentMerchant = merchants.find { it.id == selectedMerchantId } ?: merchants.firstOrNull()
    val merchantOrders = allOrders.filter { it.merchantId == currentMerchant?.id }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Orders, 1: Catalog, 2: Financials

    var showStoreSwitchDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Merchant Header & Store Selector
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { showStoreSwitchDialog = true }
                            .padding(end = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentMerchant?.let { if (language == AppLanguage.ARABIC) it.nameAr else it.nameFr } ?: "Commerce",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "▼", fontSize = 12.sp, color = BrandSecondaryBlue)
                        }
                        Text(
                            text = "${currentMerchant?.wilayaName ?: ""} • ${if (language == AppLanguage.ARABIC) "انقر للتبديل" else "Changer d'établissement"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandBlueContainer
                    ) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "مفتوح ويستقبل طلبات" else "Ouvert aux commandes",
                            color = BrandPrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (showStoreSwitchDialog) {
                    AlertDialog(
                        onDismissRequest = { showStoreSwitchDialog = false },
                        title = {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "اختر المتجر أو المطعم" else "Sélectionner l'établissement",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                merchants.forEach { m ->
                                    val isSelected = m.id == selectedMerchantId
                                    val name = if (language == AppLanguage.ARABIC) m.nameAr else m.nameFr
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setMerchantId(m.id)
                                                showStoreSwitchDialog = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.setMerchantId(m.id)
                                                    showStoreSwitchDialog = false
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = m.wilayaName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showStoreSwitchDialog = false }) {
                                Text(if (language == AppLanguage.ARABIC) "إلغاء" else "Annuler")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Merchant Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = BrandPrimaryBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "${LocalizationManager.get("merchant_orders", language)} (${merchantOrders.count { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(LocalizationManager.get("manage_catalog", language), fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(if (language == AppLanguage.ARABIC) "التقارير المالية" else "Finances", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> MerchantOrdersTab(merchantOrders = merchantOrders, viewModel = viewModel, language = language)
            1 -> MerchantCatalogTab(products = products, viewModel = viewModel, language = language)
            2 -> MerchantFinancesTab(merchantOrders = merchantOrders, language = language)
        }
    }
}

@Composable
fun MerchantOrdersTab(
    merchantOrders: List<com.example.data.model.OrderEntity>,
    viewModel: DeliveryViewModel,
    language: AppLanguage
) {
    if (merchantOrders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = if (language == AppLanguage.ARABIC) "لا توجد طلبات لهذا المتجر حالياً" else "Aucune commande pour le moment",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(merchantOrders) { order ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "№ ${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        OrderStatusBadge(status = order.status, language = language)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${order.customerName} • ${order.customerPhone}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (order.notes.isNotBlank()) {
                        Text(
                            text = "Note: ${order.notes}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = LocalizationManager.get("subtotal", language),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = LocalizationManager.formatDzd(order.subtotalDzd, language),
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimaryBlue,
                                fontSize = 15.sp
                            )
                        }

                        // Action transitions for merchant
                        when (order.status) {
                            OrderStatus.PENDING -> {
                                Button(
                                    onClick = { viewModel.merchantAcceptOrder(order.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandPrimaryBlue,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("merchant_accept_${order.id}")
                                ) {
                                    Icon(Icons.Default.Kitchen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(LocalizationManager.get("accept_order", language), fontSize = 13.sp)
                                }
                            }
                            OrderStatus.PREPARING, OrderStatus.CONFIRMED -> {
                                Button(
                                    onClick = { viewModel.merchantMarkReady(order.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandYellow,
                                        contentColor = OnBrandYellow
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
                                    modifier = Modifier.testTag("merchant_mark_ready_${order.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = OnBrandYellow)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(LocalizationManager.get("mark_ready", language), fontSize = 13.sp, color = OnBrandYellow, fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {
                                Text(
                                    text = order.driverName?.let { "Livreur: $it" } ?: "En attente assignation",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantCatalogTab(
    products: List<com.example.data.model.ProductEntity>,
    viewModel: DeliveryViewModel,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(products) { product ->
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
                            text = LocalizationManager.formatDzd(product.priceDzd, language),
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryBlue,
                            fontSize = 13.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (product.isAvailable)
                                LocalizationManager.get("product_in_stock", language)
                            else
                                LocalizationManager.get("product_out_of_stock", language),
                            fontSize = 12.sp,
                            color = if (product.isAvailable) StatusSuccess else Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = product.isAvailable,
                            onCheckedChange = { viewModel.toggleProductAvailability(product) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandYellow,
                                checkedTrackColor = BrandPrimaryBlue
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantFinancesTab(
    merchantOrders: List<com.example.data.model.OrderEntity>,
    language: AppLanguage
) {
    val totalRevenue = merchantOrders.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.subtotalDzd }
    val platformCommission = totalRevenue * 0.12 // 12% commission
    val netSettlement = totalRevenue - platformCommission

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandPrimaryBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "صافي مستحقات التاجر (DZD)" else "Revenus Nets à Régler",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LocalizationManager.formatDzd(netSettlement, language),
                        color = BrandYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC)
                            "إجمالي المبيعات: ${LocalizationManager.formatDzd(totalRevenue, language)} • عمولة المنصة (12%): ${LocalizationManager.formatDzd(platformCommission, language)}"
                        else
                            "Volume Brut: ${LocalizationManager.formatDzd(totalRevenue, language)} • Commission (12%): ${LocalizationManager.formatDzd(platformCommission, language)}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
