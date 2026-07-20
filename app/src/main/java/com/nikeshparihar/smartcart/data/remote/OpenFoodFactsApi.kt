package com.nikeshparihar.smartcart.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lightweight client for the OpenFoodFacts public API.
 * No API key required — fully open source and free.
 * Docs: https://wiki.openfoodfacts.org/API
 */
object OpenFoodFactsApi {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Look up a product by its barcode (EAN / UPC / etc).
     * Returns a user-friendly product name, or null if not found.
     */
    suspend fun lookupBarcode(barcode: String): ProductResult? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://world.openfoodfacts.org/api/v2/product/$barcode.json?fields=product_name,brands,categories_tags")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "ShoppingListApp/1.0 (Android)")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val body = connection.inputStream.bufferedReader().readText()
                val response = json.decodeFromString<OpenFoodFactsResponse>(body)
                if (response.status == 1 && response.product != null) {
                    val name = response.product.product_name?.trim()
                    val brand = response.product.brands?.trim()
                    val category = response.product.categories_tags?.firstOrNull()
                        ?.removePrefix("en:")
                        ?.replace("-", " ")
                        ?.replaceFirstChar { it.uppercase() }

                    val displayName = when {
                        !name.isNullOrBlank() && !brand.isNullOrBlank() -> "$name ($brand)"
                        !name.isNullOrBlank() -> name
                        !brand.isNullOrBlank() -> brand
                        else -> null
                    }
                    if (displayName != null) ProductResult(displayName, category ?: "") else null
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Serializable
    data class OpenFoodFactsResponse(
        val status: Int = 0,
        val product: Product? = null
    )

    @Serializable
    data class Product(
        val product_name: String? = null,
        val brands: String? = null,
        val categories_tags: List<String>? = null
    )

    data class ProductResult(
        val name: String,
        val category: String
    )
}
