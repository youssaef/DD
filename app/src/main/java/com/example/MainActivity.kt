package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.UserRole
import com.example.ui.admin.AdminPortal
import com.example.ui.components.DzTopBar
import com.example.ui.customer.CustomerPortal
import com.example.ui.driver.DriverPortal
import com.example.ui.merchant.MerchantPortal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DeliveryViewModel
import com.example.ui.viewmodel.DeliveryViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DzDeliveryApplication
        val repository = app.repository

        setContent {
            MyApplicationTheme {
                val viewModel: DeliveryViewModel = viewModel(
                    factory = DeliveryViewModelFactory(repository)
                )

                val language by viewModel.language.collectAsState()
                val currentRole by viewModel.currentRole.collectAsState()
                val wilayas by viewModel.allWilayas.collectAsState()
                val selectedWilayaCode by viewModel.selectedWilayaCode.collectAsState()

                // Support real Arabic RTL and French LTR layouts
                CompositionLocalProvider(LocalLayoutDirection provides language.layoutDirection) {
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            DzTopBar(
                                currentLanguage = language,
                                currentRole = currentRole,
                                wilayas = wilayas,
                                selectedWilayaCode = selectedWilayaCode,
                                onLanguageToggle = { viewModel.toggleLanguage() },
                                onRoleSelect = { viewModel.switchRole(it) },
                                onWilayaSelect = { viewModel.selectWilaya(it) }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentRole) {
                                UserRole.CUSTOMER -> CustomerPortal(viewModel = viewModel)
                                UserRole.DRIVER -> DriverPortal(viewModel = viewModel)
                                UserRole.MERCHANT -> MerchantPortal(viewModel = viewModel)
                                UserRole.ADMIN -> AdminPortal(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
