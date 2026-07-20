package com.nikeshparihar.smartcart.model

data class ShoppingList(
    val id: String,
    val name: String,
    val sharedWithEmail: String? = null,
    val items: List<ShoppingItem> = emptyList(),
)
