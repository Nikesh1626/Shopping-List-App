package com.nikeshparihar.smartcart.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RemoteShoppingList(
    val id: String,
    val name: String,
    val owner_id: String = "",
    val shared_with_email: String? = null,
    val is_deleted: Boolean,
    val updated_at: Long
)

@Serializable
data class RemoteShoppingItem(
    val id: String,
    val list_id: String,
    val name: String,
    val quantity: Int?,
    val category: String,
    val is_purchased: Boolean,
    val is_deleted: Boolean,
    val updated_at: Long
)
