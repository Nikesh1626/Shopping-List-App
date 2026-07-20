package com.nikeshparihar.smartcart.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ShoppingListWithItems(
    @Embedded val list: ShoppingListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "listId"
    )
    val items: List<ShoppingItemEntity>
)
