package com.example.ui.admin

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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
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
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.DeliveryViewModel

@Composable
fun AdminPortal(viewModel: DeliveryViewModel) {
    val language by viewModel.language.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val allDrivers by viewModel.allDrivers.collectAsState()
    val allMerchants by viewModel.allMerchants.collectAsState()
    val allWilayas by viewModel.allWilayas.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Operations Center, 1: Dispatch Console, 2: Wilayas & Pricing

    val totalGmv = allOrders.sumOf { it.totalDzd }
    val activeOrdersCount = allOrders.count { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
    val onlineDriversCount = allDrivers.count { it.isOnline }

    var reassignDialogOrder by remember { mutableStateOf<OrderEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BrandPrimaryBlue
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(LocalizationManager.get("admin_dashboard", language), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("${LocalizationManager.get("dispatch_console", language)} ($activeOrdersCount)", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if (language == AppLanguage.ARABIC) "الولايات والتسعير" else "Wilayas & Tarifs", fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> AdminKpiTab(
                totalGmv = totalGmv,
                activeOrders = activeOrdersCount,
                onlineDrivers = onlineDriversCount,
                merchantsCount = allMerchants.size,
                language = language
            )
            1 -> AdminDispatchTab(
                orders = allOrders,
                onReassign = { reassignDialogOrder = it },
                onCancel = { orderId -> viewModel.adminCancelOrder(orderId, "Annulée par l'administrateur") },
                language = language
            )
            2 -> AdminWilayasTab(wilayas = allWilayas, language = language)
        }
    }

    // Driver Reassignment Dialog
    reassignDialogOrder?.let { order ->
        AlertDialog(
            onDismissRequest = { reassignDialogOrder = null },
            title = {
                Text(
                    text = if (language == AppLanguage.ARABIC) "إعادة تعيين سائق للطلب ${order.id}" else "Assigner un livreur - ${order.id}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allDrivers.forEach { driver ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.adminAssignDriver(order.id, driver.id)
                                    reassignDialogOrder = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = driver.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = driver.vehicleType, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = if (driver.isOnline) "En ligne" else "Hors ligne",
                                    color = if (driver.isOnline) StatusSuccess else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { reassignDialogOrder = null }) {
                    Text(if (language == AppLanguage.ARABIC) "إغلاق" else "Fermer")
                }
            }
        )
    }
}

@Composable
fun AdminKpiTab(
    totalGmv: Double,
    activeOrders: Int,
    onlineDrivers: Int,
    merchantsCount: Int,
    language: AppLanguage
) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BrandYellow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LocalizationManager.get("platform_gmv", language),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LocalizationManager.formatDzd(totalGmv, language),
                        color = BrandYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiMetricCard(
                    title = LocalizationManager.get("active_orders_count", language),
                    value = "$activeOrders",
                    color = BrandSecondaryBlue,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = LocalizationManager.get("active_drivers_count", language),
                    value = "$onlineDrivers",
                    color = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = LocalizationManager.get("merchants_count", language),
                    value = "$merchantsCount",
                    color = BrandPrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "حالة الخوادم وأمان المعاملات" else "Sécurité & Infrastructure Système",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Supabase PostgreSQL & Row Level Security (RLS) Vérifié\n• Algorithme OTP: 4 Chiffres cryptographiques anti-fraude\n• Passerelle de paiement: Algérie Poste (Edahabia / BaridiMob Sandbox Active)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun KpiMetricCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AdminDispatchTab(
    orders: List<OrderEntity>,
    onReassign: (OrderEntity) -> Unit,
    onCancel: (String) -> Unit,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orders) { order ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
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
                    Text(text = "Client: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp)
                    Text(text = "Commerce: ${order.merchantName}", fontSize = 12.sp)
                    Text(
                        text = "Livreur: ${order.driverName ?: if (language == AppLanguage.ARABIC) "غير معين بعد" else "Non assigné"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (order.driverName != null) BrandPrimaryBlue else StatusWarning
                    )
                    Text(text = "Total: ${LocalizationManager.formatDzd(order.totalDzd, language)} (${order.paymentMethod.name})", fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                            OutlinedButton(
                                onClick = { onCancel(order.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(if (language == AppLanguage.ARABIC) "إلغاء الطلب" else "Annuler", color = StatusError, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onReassign(order) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandPrimaryBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(if (language == AppLanguage.ARABIC) "تعيين سائق" else "Assigner", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWilayasTab(
    wilayas: List<com.example.data.model.WilayaEntity>,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(wilayas) { wilaya ->
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
                    Column {
                        Text(
                            text = "${wilaya.code} - ${if (language == AppLanguage.ARABIC) wilaya.nameAr else wilaya.nameFr}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (language == AppLanguage.ARABIC) "مغطاة بالخدمة" else "Zone desservie",
                            fontSize = 11.sp,
                            color = StatusSuccess
                        )
                    }
                    Text(
                        text = LocalizationManager.formatDzd(wilaya.baseDeliveryFeeDzd, language),
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimaryBlue,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
