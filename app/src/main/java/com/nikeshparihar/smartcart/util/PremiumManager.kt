package com.nikeshparihar.smartcart.util

import android.content.Context
import android.content.SharedPreferences
import com.nikeshparihar.smartcart.billing.BillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

class PremiumManager(
    context: Context,
    applicationScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)

    val billingManager = BillingManager(context.applicationContext, applicationScope)
    
    val isPremium: StateFlow<Boolean> = billingManager.isPremium

    // --- Sharing Limits ---
    fun canShareList(currentSharedCount: Int, isCurrentlyShared: Boolean): Boolean {
        if (isPremium.value) return true
        
        // Return false if they already have 1 or more shared lists AND this list isn't already the shared one
        return currentSharedCount < 1 || isCurrentlyShared
    }

    // --- Barcode Limits ---
    fun canScanBarcode(): Boolean {
        if (isPremium.value) return true
        
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val savedMonth = prefs.getInt("scan_month", -1)
        
        if (savedMonth != currentMonth) {
            // Reset for new month
            prefs.edit()
                .putInt("scan_month", currentMonth)
                .putInt("scan_count", 0)
                .apply()
            return true
        }

        val count = prefs.getInt("scan_count", 0)
        return count < 10
    }

    fun incrementBarcodeScan() {
        if (isPremium.value) return
        
        val count = prefs.getInt("scan_count", 0)
        prefs.edit().putInt("scan_count", count + 1).apply()
    }
}
