package com.nikeshparihar.smartcart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val ownerId: String = "",
    val sharedWithEmail: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
