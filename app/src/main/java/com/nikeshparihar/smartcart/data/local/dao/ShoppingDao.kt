package com.nikeshparihar.smartcart.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nikeshparihar.smartcart.data.local.entity.ShoppingListEntity
import com.nikeshparihar.smartcart.data.local.entity.ShoppingItemEntity
import com.nikeshparihar.smartcart.data.local.entity.ShoppingListWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE isDeleted = 0")
    fun getActiveListsWithItems(): Flow<List<ShoppingListWithItems>>
    
    // For syncing
    @Query("SELECT * FROM shopping_lists WHERE isSynced = 0")
    suspend fun getUnsyncedLists(): List<ShoppingListEntity>

    @Query("SELECT * FROM shopping_items WHERE isSynced = 0")
    suspend fun getUnsyncedItems(): List<ShoppingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceList(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceLists(lists: List<ShoppingListEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceItem(item: ShoppingItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceItems(items: List<ShoppingItemEntity>)

    @Query("UPDATE shopping_items SET isPurchased = :isPurchased, isSynced = 0, updatedAt = :timestamp WHERE id = :itemId")
    suspend fun updateItemPurchasedStatus(itemId: String, isPurchased: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE shopping_lists SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :listId")
    suspend fun markListAsDeleted(listId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE shopping_items SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :itemId")
    suspend fun markItemAsDeleted(itemId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE shopping_items SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE listId = :listId")
    suspend fun markItemsAsDeletedForList(listId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM shopping_items WHERE isDeleted = 1")
    fun getDeletedItems(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE isDeleted = 1")
    suspend fun getDeletedItemsList(): List<ShoppingItemEntity>
}
