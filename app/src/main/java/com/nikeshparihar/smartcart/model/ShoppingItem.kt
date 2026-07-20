package com.nikeshparihar.smartcart.model

data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: Int?,
    val category: String,
    val isPurchased: Boolean = false,
)
