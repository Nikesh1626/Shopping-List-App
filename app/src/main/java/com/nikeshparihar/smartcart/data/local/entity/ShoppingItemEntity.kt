package com.nikeshparihar.smartcart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val name: String,
    val quantity: Int?,
    val category: String,
    val isPurchased: Boolean,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
