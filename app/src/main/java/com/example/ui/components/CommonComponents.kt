package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WilayaEntity
import com.example.domain.AppLanguage
import com.example.domain.LocalizationManager
import com.example.domain.OrderStatus
import com.example.domain.UserRole
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandPageBackground
import com.example.ui.theme.BrandPrimaryBlue
import com.example.ui.theme.BrandSecondaryBlue
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandTextPrimary
import com.example.ui.theme.BrandYellow
import com.example.ui.theme.BrandYellowContainer
import com.example.ui.theme.BrandYellowDark
import com.example.ui.theme.BrandYellowHover
import com.example.ui.theme.OnBrandYellow
import com.example.ui.theme.OnBrandYellowContainer
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusInfoBg
import com.example.ui.theme.StatusInfoText
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DzTopBar(
    currentLanguage: AppLanguage,
    currentRole: UserRole,
    wilayas: List<WilayaEntity>,
    selectedWilayaCode: String,
    onLanguageToggle: () -> Unit,
    onRoleSelect: (UserRole) -> Unit,
    onWilayaSelect: (String) -> Unit
) {
    var showRoleDialog by remember { mutableStateOf(false) }
    var showWilayaDialog by remember { mutableStateOf(false) }

    val activeWilaya = wilayas.find { it.code == selectedWilayaCode }
    val wilayaDisplay = if (activeWilaya != null) {
        if (currentLanguage == AppLanguage.ARABIC) activeWilaya.nameAr else activeWilaya.nameFr
    } else "Alger (16)"

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BrandPrimaryBlue,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Stylized brand accent pill
                    Box(
                        modifier = Modifier
                            .size(16.dp, 12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(BrandYellow)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f).height(12.dp).background(BrandYellow))
                            Box(modifier = Modifier.weight(1f).height(12.dp).background(BrandSecondaryBlue))
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = LocalizationManager.get("app_name", currentLanguage),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                // Clickable Wilaya badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showWilayaDialog = true }
                        .padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Wilaya",
                        tint = BrandYellow,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$wilayaDisplay ▼",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        },
        actions = {
            // Language switch chip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clickable { onLanguageToggle() }
                    .testTag("language_toggle_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Change Language",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "Français" else "العربية",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Role Switcher Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BrandYellow,
                modifier = Modifier
                    .clickable { showRoleDialog = true }
                    .testTag("role_switcher_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val roleIcon = when (currentRole) {
                        UserRole.CUSTOMER -> Icons.Default.Person
                        UserRole.DRIVER -> Icons.Default.DeliveryDining
                        UserRole.MERCHANT -> Icons.Default.Storefront
                        UserRole.ADMIN -> Icons.Default.Tune
                    }
                    val roleLabel = when (currentRole) {
                        UserRole.CUSTOMER -> LocalizationManager.get("role_customer", currentLanguage)
                        UserRole.DRIVER -> LocalizationManager.get("role_driver", currentLanguage)
                        UserRole.MERCHANT -> LocalizationManager.get("role_merchant", currentLanguage)
                        UserRole.ADMIN -> LocalizationManager.get("role_admin", currentLanguage)
                    }
                    Icon(
                        imageVector = roleIcon,
                        contentDescription = roleLabel,
                        tint = OnBrandYellow,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = roleLabel,
                        color = OnBrandYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )

    if (showRoleDialog) {
        RoleSelectionDialog(
            currentRole = currentRole,
            currentLanguage = currentLanguage,
            onDismiss = { showRoleDialog = false },
            onSelect = {
                onRoleSelect(it)
                showRoleDialog = false
            }
        )
    }

    if (showWilayaDialog) {
        WilayaSelectionDialog(
            wilayas = wilayas,
            selectedCode = selectedWilayaCode,
            currentLanguage = currentLanguage,
            onDismiss = { showWilayaDialog = false },
            onSelect = {
                onWilayaSelect(it)
                showWilayaDialog = false
            }
        )
    }
}

@Composable
fun RoleSelectionDialog(
    currentRole: UserRole,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (UserRole) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = LocalizationManager.get("select_role", currentLanguage),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UserRole.entries.forEach { role ->
                    val isSelected = role == currentRole
                    val title = when (role) {
                        UserRole.CUSTOMER -> LocalizationManager.get("role_customer", currentLanguage)
                        UserRole.DRIVER -> LocalizationManager.get("role_driver", currentLanguage)
                        UserRole.MERCHANT -> LocalizationManager.get("role_merchant", currentLanguage)
                        UserRole.ADMIN -> LocalizationManager.get("role_admin", currentLanguage)
                    }
                    val desc = when (role) {
                        UserRole.CUSTOMER -> if (currentLanguage == AppLanguage.ARABIC) "تصفح المتاجر، إضافة للسلة، الطلب وتتبع المسار" else "Explorer commerces, panier, commande & suivi"
                        UserRole.DRIVER -> if (currentLanguage == AppLanguage.ARABIC) "استلام الطلبات، توصيل للمنازل وتأكيد OTP والأرباح" else "Courses disponibles, retrait, livraison OTP & gains"
                        UserRole.MERCHANT -> if (currentLanguage == AppLanguage.ARABIC) "إدارة الطلبات الواردة، تحضير، والتحكم في الأسعار" else "Gestion des commandes, préparation cuisine & menu"
                        UserRole.ADMIN -> if (currentLanguage == AppLanguage.ARABIC) "مراقبة الأسطول، إحصائيات المنصة، التسعير والتوزيع" else "Opérations nationales, dispatch, statistiques DZ"
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(role) }
                            .testTag("role_option_${role.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { onSelect(role) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLanguage == AppLanguage.ARABIC) "إغلاق" else "Fermer")
            }
        }
    )
}

@Composable
fun WilayaSelectionDialog(
    wilayas: List<WilayaEntity>,
    selectedCode: String,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentLanguage == AppLanguage.ARABIC) "اختر ولاية التوصيل" else "Sélectionner la Wilaya",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                wilayas.forEach { wilaya ->
                    val isSelected = wilaya.code == selectedCode
                    val name = if (currentLanguage == AppLanguage.ARABIC)
                        "${wilaya.code} - ${wilaya.nameAr} (${wilaya.baseDeliveryFeeDzd.toInt()} د.ج)"
                    else
                        "${wilaya.code} - ${wilaya.nameFr} (${wilaya.baseDeliveryFeeDzd.toInt()} DZD)"

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) BrandBlueContainer else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(wilaya.code) }
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandPrimaryBlue else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLanguage == AppLanguage.ARABIC) "إلغاء" else "Annuler")
            }
        }
    )
}

@Composable
fun OrderStatusBadge(status: OrderStatus, language: AppLanguage) {
    val (bg, fg) = when (status) {
        OrderStatus.PENDING -> Pair(StatusWarningBg, StatusWarningText)
        OrderStatus.CONFIRMED, OrderStatus.PREPARING -> Pair(BrandBlueContainer, BrandPrimaryBlue)
        OrderStatus.READY_FOR_PICKUP -> Pair(BrandYellowContainer, OnBrandYellowContainer)
        OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.DELIVERING -> Pair(StatusInfoBg, StatusInfoText)
        OrderStatus.DELIVERED -> Pair(StatusSuccessBg, StatusSuccessText)
        OrderStatus.CANCELLED -> Pair(StatusErrorBg, StatusErrorText)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName(language),
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Interactive Vector Map canvas showing streets, merchant pin, customer destination,
 * animated dashed trajectory, and moving courier scooter!
 */
@Composable
fun InteractiveMapCanvas(
    modifier: Modifier = Modifier,
    status: OrderStatus,
    merchantName: String,
    customerAddress: String,
    driverName: String?
) {
    val transition = rememberInfiniteTransition(label = "scooterMove")
    val progress by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandPageBackground),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Draw background roads grid
                val roadColor = BrandBorder
                drawLine(roadColor, Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = 14f)
                drawLine(roadColor, Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 18f)
                drawLine(roadColor, Offset(w * 0.3f, 0f), Offset(w * 0.3f, h), strokeWidth = 12f)
                drawLine(roadColor, Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 16f)

                // Start point: Merchant
                val start = Offset(w * 0.2f, h * 0.7f)
                // Mid waypoints
                val mid1 = Offset(w * 0.3f, h * 0.35f)
                val mid2 = Offset(w * 0.75f, h * 0.35f)
                // End point: Customer
                val end = Offset(w * 0.82f, h * 0.7f)

                // Route Path
                val routePath = Path().apply {
                    moveTo(start.x, start.y)
                    lineTo(mid1.x, mid1.y)
                    lineTo(mid2.x, mid2.y)
                    lineTo(end.x, end.y)
                }

                // Draw route line
                drawPath(
                    path = routePath,
                    color = BrandSecondaryBlue,
                    style = Stroke(
                        width = 8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                )

                // Draw Merchant Pin (Primary Blue circle)
                drawCircle(color = BrandPrimaryBlue, radius = 14f, center = start)
                drawCircle(color = Color.White, radius = 6f, center = start)

                // Draw Customer Pin (Yellow circle with dark border)
                drawCircle(color = BrandYellow, radius = 14f, center = end)
                drawCircle(color = BrandPrimaryBlue, radius = 6f, center = end)

                // Determine scooter position along route
                val scooterPos = when {
                    status == OrderStatus.DELIVERED -> end
                    status == OrderStatus.READY_FOR_PICKUP || status == OrderStatus.ASSIGNED -> start
                    status == OrderStatus.DELIVERING -> {
                        when {
                            progress < 0.33f -> {
                                val t = progress / 0.33f
                                Offset(start.x + (mid1.x - start.x) * t, start.y + (mid1.y - start.y) * t)
                            }
                            progress < 0.66f -> {
                                val t = (progress - 0.33f) / 0.33f
                                Offset(mid1.x + (mid2.x - mid1.x) * t, mid1.y + (mid2.y - mid1.y) * t)
                            }
                            else -> {
                                val t = (progress - 0.66f) / 0.34f
                                Offset(mid2.x + (end.x - mid2.x) * t, mid2.y + (end.y - mid2.y) * t)
                            }
                        }
                    }
                    else -> start
                }

                // Draw Driver Scooter indicator (Brand Yellow with Blue center)
                drawCircle(color = BrandYellow, radius = 16f, center = scooterPos)
                drawCircle(color = BrandPrimaryBlue, radius = 8f, center = scooterPos)
                drawCircle(color = Color.White, radius = 3f, center = scooterPos)
            }

            // Overlay chips
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandSurface.copy(alpha = 0.95f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BrandPrimaryBlue))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = merchantName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandTextPrimary)
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandSurface.copy(alpha = 0.95f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BrandYellow))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = customerAddress.take(22) + "...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandTextPrimary)
            }
        }
    }
}
