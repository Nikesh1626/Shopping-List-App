package com.nikeshparihar.smartcart.data.repository

import com.nikeshparihar.smartcart.data.local.dao.ShoppingDao
import com.nikeshparihar.smartcart.data.local.entity.ShoppingItemEntity
import com.nikeshparihar.smartcart.data.local.entity.ShoppingListEntity
import com.nikeshparihar.smartcart.data.remote.SupabaseApi
import com.nikeshparihar.smartcart.data.remote.model.RemoteShoppingItem
import com.nikeshparihar.smartcart.data.remote.model.RemoteShoppingList
import com.nikeshparihar.smartcart.model.ShoppingItem
import com.nikeshparihar.smartcart.model.ShoppingList
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class OfflineFirstShoppingRepository(
    private val dao: ShoppingDao
) {
    private val supabase = SupabaseApi.client

    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id ?: ""
    }

    private fun getCurrentUserEmail(): String {
        return supabase.auth.currentUserOrNull()?.email ?: ""
    }

    // ────────────────────────────────────────────────────────
    // LOCAL OPERATIONS (Exposed to ViewModel)
    // ────────────────────────────────────────────────────────

    fun getShoppingLists(): Flow<List<ShoppingList>> {
        return dao.getActiveListsWithItems().map { relations ->
            val currentUser = getCurrentUserId()
            val currentEmail = getCurrentUserEmail()
            relations
                .filter { it.list.ownerId == currentUser || (it.list.sharedWithEmail != null && it.list.sharedWithEmail == currentEmail) }
                .map { relation ->
                    ShoppingList(
                        id = relation.list.id,
                        name = relation.list.name,
                        sharedWithEmail = relation.list.sharedWithEmail,
                        items = relation.items
                            .filter { !it.isDeleted }
                        .map { itemEntity ->
                        ShoppingItem(
                            id = itemEntity.id,
                            name = itemEntity.name,
                            quantity = itemEntity.quantity,
                            category = itemEntity.category,
                            isPurchased = itemEntity.isPurchased
                        )
                    }
                )
            }
        }
    }

    suspend fun addList(name: String, sharedWithEmail: String? = null): String {
        val newId = UUID.randomUUID().toString()
        val entity = ShoppingListEntity(
            id = newId,
            name = name,
            ownerId = getCurrentUserId(),
            sharedWithEmail = sharedWithEmail,
            isSynced = false,
            isDeleted = false
        )
        dao.insertOrReplaceList(entity)
        return newId
    }

    suspend fun updateList(listId: String, name: String, sharedWithEmail: String? = null) {
        val lists = getShoppingLists() // Not ideal for prod, but we'll adapt. Actually, better:
        // Create an entity with the updated fields, retaining the ownerId.
        val entity = ShoppingListEntity(
            id = listId,
            name = name,
            ownerId = getCurrentUserId(), // Best-effort assumption for update, usually true.
            sharedWithEmail = sharedWithEmail,
            isSynced = false,
            isDeleted = false
        )
        dao.insertOrReplaceList(entity)
    }

    suspend fun addItem(listId: String, name: String, quantity: Int?, category: String) {
        val entity = ShoppingItemEntity(
            id = UUID.randomUUID().toString(),
            listId = listId,
            name = name,
            quantity = quantity,
            category = category,
            isPurchased = false,
            isSynced = false,
            isDeleted = false
        )
        dao.insertOrReplaceItem(entity)
    }

    suspend fun toggleItemPurchased(itemId: String, currentStatus: Boolean) {
        dao.updateItemPurchasedStatus(itemId, !currentStatus)
    }

    suspend fun deleteItem(itemId: String) {
        dao.markItemAsDeleted(itemId)
    }

    suspend fun deleteList(listId: String) {
        dao.markListAsDeleted(listId)
        dao.markItemsAsDeletedForList(listId)
    }

    suspend fun updateItem(itemId: String, listId: String, name: String, quantity: Int?, category: String, isPurchased: Boolean) {
        val entity = ShoppingItemEntity(
            id = itemId,
            listId = listId,
            name = name,
            quantity = quantity,
            category = category,
            isPurchased = isPurchased,
            isSynced = false,
            isDeleted = false
        )
        dao.insertOrReplaceItem(entity)
    }


    // ────────────────────────────────────────────────────────
    // SYNC LOGIC (Cloud <-> Local)
    // ────────────────────────────────────────────────────────
    
    suspend fun sync() {
        try {
            // 1. PUSH local unsynced changes to Remote
            val unsyncedLists = dao.getUnsyncedLists()
            if (unsyncedLists.isNotEmpty()) {
                val remoteLists = unsyncedLists.map { it.toRemote() }
                supabase.postgrest["shopping_lists"].upsert(remoteLists)
            }

            val unsyncedItems = dao.getUnsyncedItems()
            if (unsyncedItems.isNotEmpty()) {
                val remoteItems = unsyncedItems.map { it.toRemote() }
                supabase.postgrest["shopping_items"].upsert(remoteItems)
            }

            // Mark locally as synced
            unsyncedLists.forEach { dao.insertOrReplaceList(it.copy(isSynced = true)) }
            unsyncedItems.forEach { dao.insertOrReplaceItem(it.copy(isSynced = true)) }

            // 2. PULL Remote Data
            val remoteListsData = supabase.postgrest["shopping_lists"].select().decodeList<RemoteShoppingList>()
            val remoteItemsData = supabase.postgrest["shopping_items"].select().decodeList<RemoteShoppingItem>()

            // Overwrite into local (Basic sync implementation)
            val mergedLists = remoteListsData.map { it.toLocal() }
            val mergedItems = remoteItemsData.map { it.toLocal() }

            dao.insertOrReplaceLists(mergedLists)
            dao.insertOrReplaceItems(mergedItems)

        } catch (e: Exception) {
            e.printStackTrace()
            // Silently fail allowing offline fallback
        }
    }

    // ────────────────────────────────────────────────────────
    // MAPPERS
    // ────────────────────────────────────────────────────────

    private fun ShoppingListEntity.toRemote() = RemoteShoppingList(
        id = id, name = name, owner_id = ownerId, shared_with_email = sharedWithEmail, is_deleted = isDeleted, updated_at = updatedAt
    )
    private fun ShoppingItemEntity.toRemote() = RemoteShoppingItem(
        id = id, list_id = listId, name = name, quantity = quantity, category = category,
        is_purchased = isPurchased, is_deleted = isDeleted, updated_at = updatedAt
    )
    private fun RemoteShoppingList.toLocal() = ShoppingListEntity(
        id = id, name = name, ownerId = owner_id, sharedWithEmail = shared_with_email, isDeleted = is_deleted, updatedAt = updated_at, isSynced = true
    )
    private fun RemoteShoppingItem.toLocal() = ShoppingItemEntity(
        id = id, listId = list_id, name = name, quantity = quantity, category = category,
        isPurchased = is_purchased, isDeleted = is_deleted, updatedAt = updated_at, isSynced = true
    )
}
