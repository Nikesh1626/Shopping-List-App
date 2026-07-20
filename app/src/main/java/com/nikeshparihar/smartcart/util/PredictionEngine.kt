package com.nikeshparihar.smartcart.util

import com.nikeshparihar.smartcart.data.local.entity.ShoppingItemEntity
import java.util.concurrent.TimeUnit

data class PredictionResult(
    val itemName: String,
    val category: String,
    val score: Double // Higher score means higher probability of being "Due"
)

object PredictionEngine {

    /**
     * Logic:
     * 1. Group items by lowercase name.
     * 2. Calculate average time delta between purchases.
     * 3. If current_time - last_purchase_time > 80% of average_delta -> Suggest!
     */
    fun getSuggestions(
        history: List<ShoppingItemEntity>,
        activeItemNames: Set<String>
    ): List<PredictionResult> {
        if (history.isEmpty()) return emptyList()

        // Group by normalized name
        val grouped = history.groupBy { it.name.trim().lowercase() }
        
        val suggestions = mutableListOf<PredictionResult>()
        val currentTime = System.currentTimeMillis()

        for ((name, items) in grouped) {
            // Skip if already in active shopping lists
            if (activeItemNames.contains(name)) continue
            
            // Need at least 2 data points (1 interval) to predict
            if (items.size < 2) continue

            // Sort by time
            val sortedItems = items.sortedBy { it.updatedAt }
            
            // Calculate intervals (in milliseconds)
            val intervals = mutableListOf<Long>()
            for (i in 0 until sortedItems.size - 1) {
                val delta = sortedItems[i + 1].updatedAt - sortedItems[i].updatedAt
                // Filter out anomalies (e.g. buying twice in 1 hour)
                if (delta > TimeUnit.HOURS.toMillis(12)) {
                    intervals.add(delta)
                }
            }

            if (intervals.isEmpty()) continue

            val averageInterval = intervals.average()
            val lastPurchaseTime = sortedItems.last().updatedAt
            val timeSinceLastPurchase = currentTime - lastPurchaseTime

            // Scoring: 0.0 (just bought) to 1.0 (due) and beyond
            val score = timeSinceLastPurchase / averageInterval

            // Suggest if we are at least 80% through the cycle
            if (score >= 0.8) {
                suggestions.add(
                    PredictionResult(
                        itemName = sortedItems.last().name, // Use last known proper casing
                        category = sortedItems.last().category,
                        score = score
                    )
                )
            }
        }

        return suggestions.sortedByDescending { it.score }
    }
}
