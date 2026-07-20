package com.nikeshparihar.smartcart

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nikeshparihar.smartcart.util.SmartPredictionWorker
import java.util.concurrent.TimeUnit

import com.google.android.gms.ads.MobileAds

class ShoppingApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupSmartReminders()
        
        // Initialize AdMob
        MobileAds.initialize(this) {}
    }

    private fun setupSmartReminders() {
        val workRequest = PeriodicWorkRequestBuilder<SmartPredictionWorker>(
            24, TimeUnit.HOURS, // Run once a day
            1, TimeUnit.HOURS // Window of 1 hour
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SmartShoppingReminders",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            workRequest
        )
    }
}
