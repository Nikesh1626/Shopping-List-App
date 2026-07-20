package com.nikeshparihar.smartcart.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nikeshparihar.smartcart.R
import com.nikeshparihar.smartcart.data.local.ShoppingDatabase

class SmartPredictionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = ShoppingDatabase.getDatabase(applicationContext)
        val dao = database.shoppingDao()

        // 1. Get History
        val history = dao.getDeletedItemsList()
        
        // 2. Get currently active items to exclude them
        val activeLists = dao.getUnsyncedItems() // This is a bit lazy, we need all non-deleted items.
        // Let's just get all active item names
        // Note: Ideally we'd have a specific DAO query for "all active names"
        val activeItemNames = database.shoppingDao().getUnsyncedItems()
            .map { it.name.trim().lowercase() }.toSet()

        // 3. Run Prediction
        val suggestions = PredictionEngine.getSuggestions(history, activeItemNames)

        if (suggestions.isNotEmpty()) {
            val dueItem = suggestions.first()
            sendNotification(dueItem.itemName)
        }

        return Result.success()
    }

    private fun sendNotification(itemName: String) {
        val channelId = "smart_reminders"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Smart Shopping Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies you when you are likely running out of essentials"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard android icon for now
            .setContentTitle("Shopping Reminder 🥣")
            .setContentText("You usually run out of $itemName around now. Want to add it to your list?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
