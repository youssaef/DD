package com.example.ui.driver

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.domain.AppLanguage
import com.example.domain.LocalizationManager
import com.example.domain.OrderStatus
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandPrimaryBlue
import com.example.ui.theme.BrandSecondaryBlue
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandTextPrimary
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
fun DriverPortal(viewModel: DeliveryViewModel) {
    val language by viewModel.language.collectAsState()
    val drivers by viewModel.allDrivers.collectAsState()
    val currentDriverId by viewModel.currentDriverId.collectAsState()
    val availableOrders by viewModel.availableOrdersForDrivers.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    val currentDriver = drivers.find { it.id == currentDriverId } ?: drivers.firstOrNull()

    // Find if this driver has an active order in progress
    val activeDelivery = allOrders.find {
        it.driverId == currentDriver?.id && (it.status == OrderStatus.ASSIGNED || it.status == OrderStatus.DELIVERING)
    }

    var showOtpDialogForOrder by remember { mutableStateOf<OrderEntity?>(null) }

    var showDriverSwitchDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Driver Identity & Status Switch Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showDriverSwitchDialog = true }
                                .padding(end = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BrandYellow, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentDriver?.name ?: "Livreur DZ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "▼", fontSize = 11.sp, color = BrandSecondaryBlue)
                                }
                                Text(
                                    text = "${currentDriver?.vehicleType} • ${if (language == AppLanguage.ARABIC) "تغيير السائق" else "Changer de profil"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Online / Offline Switch
                        currentDriver?.let { driver ->
                            Column(horizontalAlignment = Alignment.End) {
                                Switch(
                                    checked = driver.isOnline,
                                    onCheckedChange = { viewModel.driverToggleOnline(driver.id, driver.isOnline) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = BrandYellow,
                                        checkedTrackColor = BrandPrimaryBlue
                                    )
                                )
                                Text(
                                    text = if (driver.isOnline)
                                        LocalizationManager.get("driver_status_online", language)
                                    else
                                        LocalizationManager.get("driver_status_offline", language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (driver.isOnline) StatusSuccess else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Earnings & Deliveries Count Dashboard
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BrandSecondaryBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LocalizationManager.get("today_earnings", language),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LocalizationManager.formatDzd(currentDriver?.todayEarningsDzd ?: 0.0, language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = BrandPrimaryBlue
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LocalizationManager.get("completed_trips", language),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currentDriver?.totalDeliveries ?: 0}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Active Delivery Mission Card (If driver has accepted an order)
        if (activeDelivery != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandBlueContainer),
                    border = androidx.compose.foundation.BorderStroke(2.dp, BrandSecondaryBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "مهمة التوصيل الحالية ⚡" else "Mission en cours ⚡",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandPrimaryBlue
                            )
                            OrderStatusBadge(status = activeDelivery.status, language = language)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live map
                        InteractiveMapCanvas(
                            modifier = Modifier.fillMaxWidth(),
                            status = activeDelivery.status,
                            merchantName = activeDelivery.merchantName,
                            customerAddress = activeDelivery.customerAddress,
                            driverName = activeDelivery.driverName
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pickup Address info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = BrandSecondaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${LocalizationManager.get("pickup_from", language)}: ${activeDelivery.merchantName}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Destination info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = StatusError, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${LocalizationManager.get("deliver_to", language)}: ${activeDelivery.customerName} (${activeDelivery.customerAddress})",
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons depending on delivery state
                        if (activeDelivery.status == OrderStatus.ASSIGNED) {
                            Button(
                                onClick = { viewModel.driverConfirmPickup(activeDelivery.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandPrimaryBlue,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("driver_confirm_pickup_btn")
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(LocalizationManager.get("confirm_pickup_btn", language), fontWeight = FontWeight.Bold)
                            }
                        } else if (activeDelivery.status == OrderStatus.DELIVERING) {
                            Button(
                                onClick = { showOtpDialogForOrder = activeDelivery },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandYellow,
                                    contentColor = OnBrandYellow
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("driver_confirm_delivery_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = OnBrandYellow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(LocalizationManager.get("confirm_delivery_btn", language), fontWeight = FontWeight.Bold, color = OnBrandYellow)
                            }
                        }
                    }
                }
            }
        }

        // Available Delivery Requests section
        item {
            Text(
                text = "${LocalizationManager.get("available_requests", language)} (${availableOrders.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }

        if (availableOrders.isEmpty() && activeDelivery == null) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "لا توجد طلبات معلقة حالياً • جاري تحديث الرادار..." else "Aucune course en attente • Radar en veille...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(availableOrders) { order ->
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
                        Text(text = "№ ${order.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        // Driver compensation cut (85% of delivery fee)
                        val driverCut = order.deliveryFeeDzd * 0.85
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandBlueContainer
                        ) {
                            Text(
                                text = "+${LocalizationManager.formatDzd(driverCut, language)}",
                                color = BrandPrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Merchant
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = BrandSecondaryBlue, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${order.merchantName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Customer address
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = StatusError, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${order.customerAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { currentDriver?.let { viewModel.driverAcceptOrder(order.id, it.id) } },
                        enabled = activeDelivery == null && (currentDriver?.isOnline == true),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimaryBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("accept_order_${order.id}")
                    ) {
                        Text(
                            text = LocalizationManager.get("accept_delivery", language),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // OTP Verification Dialog
    showOtpDialogForOrder?.let { order ->
        var enteredOtp by remember { mutableStateOf("") }
        val otpError by viewModel.otpVerificationError.collectAsState()

        AlertDialog(
            onDismissRequest = {
                viewModel.clearOtpError()
                showOtpDialogForOrder = null
            },
            title = {
                Text(
                    text = LocalizationManager.get("confirm_delivery_btn", language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = LocalizationManager.get("enter_otp_prompt", language),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = {
                            if (it.length <= 4) enteredOtp = it
                            viewModel.clearOtpError()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("Ex: 5183") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("driver_otp_input")
                    )
                    otpError?.let { err ->
                        Text(text = err, color = StatusError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.driverConfirmDeliveryWithOtp(order.id, enteredOtp)
                        if (enteredOtp.trim() == order.otpCode.trim()) {
                            showOtpDialogForOrder = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandYellow,
                        contentColor = OnBrandYellow
                    ),
                    modifier = Modifier.testTag("driver_submit_otp_btn")
                ) {
                    Text(if (language == AppLanguage.ARABIC) "تأكيد" else "Valider", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.clearOtpError()
                    showOtpDialogForOrder = null
                }) {
                    Text(if (language == AppLanguage.ARABIC) "إلغاء" else "Annuler")
                }
            }
        )
    }

    if (showDriverSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showDriverSwitchDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.ARABIC) "اختر حساب السائق" else "Sélectionner le profil livreur",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    drivers.forEach { drv ->
                        val isSelected = drv.id == currentDriverId
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDriverId(drv.id)
                                    showDriverSwitchDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setDriverId(drv.id)
                                        showDriverSwitchDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = drv.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "${drv.vehicleType} • Wilaya ${drv.wilayaCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDriverSwitchDialog = false }) {
                    Text(if (language == AppLanguage.ARABIC) "إلغاء" else "Annuler")
                }
            }
        )
    }
}
