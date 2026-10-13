package com.example

import android.app.Application
import com.example.data.local.DzDeliveryDatabase
import com.example.data.repository.DzDeliveryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class DzDeliveryApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { DzDeliveryDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { DzDeliveryRepository(database) }

    override fun onCreate() {
        super.onCreate()
        // Trigger DB creation & initial seeding
        database
    }
}
