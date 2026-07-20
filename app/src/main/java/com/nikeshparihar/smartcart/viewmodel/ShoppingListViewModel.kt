package com.nikeshparihar.smartcart.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nikeshparihar.smartcart.data.local.ShoppingDatabase
import com.nikeshparihar.smartcart.data.repository.OfflineFirstShoppingRepository
import com.nikeshparihar.smartcart.model.ShoppingItem
import com.nikeshparihar.smartcart.model.ShoppingList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.nikeshparihar.smartcart.data.remote.SupabaseRealtimeSync

data class AppUiState(
    val operatingListId: String? = null,
    val isListDialogVisible: Boolean = false,
    val draftListName: String = "",
    val draftSharedEmail: String = "",
    val editingListId: String? = null,

    val isItemDialogVisible: Boolean = false,
    val draftName: String = "",
    val draftQuantity: String = "",
    val draftCategory: String = "",
    val editingItem: ShoppingItem? = null,

    // Barcode scanner
    val isScannerVisible: Boolean = false,
    val scannerListId: String? = null,
    val scannerMessage: String = "",

    // Suggestions
    val suggestions: List<com.nikeshparihar.smartcart.util.PredictionResult> = emptyList(),

    // Premium Paywall
    val isPremiumPaywallVisible: Boolean = false,
)

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ShoppingDatabase.getDatabase(application)
    private val repository = OfflineFirstShoppingRepository(database.shoppingDao())
    val premiumManager = com.nikeshparihar.smartcart.util.PremiumManager(application, viewModelScope)

    // UI state for dialogs / drafts
    var uiState by mutableStateOf(AppUiState())
        private set

    // Persistent data directly bound to the Room database
    val shoppingLists: StateFlow<List<ShoppingList>> = repository.getShoppingLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium

    init {
        // Trigger a sync when ViewModel starts (e.g. app opens)
        forceSync()

        SupabaseRealtimeSync.startPostgresChangeListener(
            client = com.nikeshparihar.smartcart.data.remote.SupabaseApi.client,
            scope = viewModelScope,
            onChange = { forceSync() },
        )

        // Listen for history changes to update suggestions
        viewModelScope.launch {
            database.shoppingDao().getDeletedItems().collect { history ->
                val activeNames = shoppingLists.value.flatMap { list -> list.items.map { it.name.lowercase().trim() } }.toSet()
                uiState = uiState.copy(
                    suggestions = com.nikeshparihar.smartcart.util.PredictionEngine.getSuggestions(history, activeNames)
                )
            }
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            repository.sync()
        }
    }

    // ── Lists ────────────────────────────────────────────────────────
    fun onAddListClick() { uiState = uiState.copy(isListDialogVisible = true, draftListName = "", draftSharedEmail = "") }
    
    fun onEditListClick(list: ShoppingList) {
        uiState = uiState.copy(
            isListDialogVisible = true,
            draftListName = list.name,
            draftSharedEmail = list.sharedWithEmail ?: "",
            editingListId = list.id
        )
    }

    fun onDraftListNameChange(name: String) { uiState = uiState.copy(draftListName = name) }
    
    fun onDraftSharedEmailChange(email: String) { uiState = uiState.copy(draftSharedEmail = email) }

    fun onConfirmListDialog() {
        if (uiState.draftListName.isBlank()) return
        val listId = uiState.editingListId
        val sharedEmail = uiState.draftSharedEmail.trim().ifEmpty { null }
        
        // Premium check for sharing
        if (sharedEmail != null) {
            val currentSharedCount = shoppingLists.value.count { it.sharedWithEmail != null }
            val isCurrentlyShared = listId != null && shoppingLists.value.find { it.id == listId }?.sharedWithEmail != null
            if (!premiumManager.canShareList(currentSharedCount, isCurrentlyShared)) {
                uiState = uiState.copy(isListDialogVisible = false, isPremiumPaywallVisible = true)
                return
            }
        }
        
        viewModelScope.launch {
            if (listId == null) {
                repository.addList(uiState.draftListName.trim(), sharedEmail)
            } else {
                // Update list (add to repo next)
                repository.updateList(listId, uiState.draftListName.trim(), sharedEmail)
            }
            repository.sync()
        }
        uiState = uiState.copy(isListDialogVisible = false, editingListId = null, draftListName = "", draftSharedEmail = "")
    }

    fun onCancelListDialog() { uiState = uiState.copy(isListDialogVisible = false, editingListId = null) }

    fun onDeleteList(listId: String) {
        viewModelScope.launch {
            repository.deleteList(listId)
            repository.sync()
        }
    }

    // ── Items ────────────────────────────────────────────────────────
    fun onAddItemClick(listId: String) {
        uiState = uiState.copy(
            operatingListId = listId,
            isItemDialogVisible = true,
            draftName = "",
            draftQuantity = "",
            draftCategory = ""
        )
    }

    fun onEditItemClick(listId: String, item: ShoppingItem) {
        uiState = uiState.copy(
            operatingListId = listId,
            isItemDialogVisible = true,
            editingItem = item,
            draftName = item.name,
            draftQuantity = item.quantity?.toString() ?: "",
            draftCategory = item.category
        )
    }

    fun onDraftNameChange(name: String) {
        val guess = guessCategory(name)
        val currentCat = uiState.draftCategory
        val newCategory = if (guess.isNotBlank() && (currentCat.isBlank() || currentCat == guessCategory(uiState.draftName))) guess else currentCat
        uiState = uiState.copy(draftName = name, draftCategory = newCategory)
    }
    fun onDraftQuantityChange(q: String) { uiState = uiState.copy(draftQuantity = q) }
    fun onDraftCategoryChange(c: String) { uiState = uiState.copy(draftCategory = c) }

    fun onConfirmItemDialog() {
        if (uiState.draftName.isBlank()) return
        val listId = uiState.operatingListId ?: return
        val editingItem = uiState.editingItem

        val itemName = uiState.draftName.trim()
        val quantity = uiState.draftQuantity.toIntOrNull()
        val category = uiState.draftCategory.trim().ifBlank { guessCategory(itemName) }

        viewModelScope.launch {
            if (editingItem == null) {
                repository.addItem(listId, itemName, quantity, category)
            } else {
                repository.updateItem(editingItem.id, listId, itemName, quantity, category, editingItem.isPurchased)
            }
            repository.sync()
        }

        uiState = uiState.copy(
            isItemDialogVisible = false,
            editingItem = null,
            draftName = "",
            draftQuantity = "",
            draftCategory = "",
        )
    }

    fun onCancelItemDialog() {
        uiState = uiState.copy(isItemDialogVisible = false, editingItem = null)
    }

    fun onDeleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteItem(item.id)
            repository.sync()
        }
    }

    fun onToggleItemPurchased(item: ShoppingItem) {
        viewModelScope.launch {
            repository.toggleItemPurchased(item.id, item.isPurchased)
            repository.sync()
        }
    }

    fun parseAndAddItem(listId: String, spokenText: String) {
        if (spokenText.isBlank()) return

        val words = spokenText.split(" ")
        var qty: Int? = null
        var name = spokenText

        val firstWord = words.firstOrNull()?.lowercase()
        if (firstWord != null) {
            val num = firstWord.toIntOrNull() ?: when (firstWord) {
                "one", "a", "an" -> 1
                "two", "to", "too" -> 2
                "three" -> 3
                "four", "for" -> 4
                "five" -> 5
                "six" -> 6
                "seven" -> 7
                "eight" -> 8
                "nine" -> 9
                "ten" -> 10
                else -> null
            }
            if (num != null) {
                qty = num
                name = words.drop(1).joinToString(" ")
            }
        }

        val finalName = name.trim().replaceFirstChar { it.uppercase() }
        val category = guessCategory(finalName)

        viewModelScope.launch {
            repository.addItem(listId, finalName, qty, category)
            repository.sync()
        }
    }

    fun handleAddFromAssistant(itemName: String, listName: String? = null) {
        viewModelScope.launch {
            val allLists = shoppingLists.value
            
            val targetListId = if (listName != null) {
                // Try to find a list that matches the user's spoken list name
                allLists.find { it.name.equals(listName, ignoreCase = true) }?.id 
                    ?: repository.addList(listName.trim().replaceFirstChar { it.uppercase() })
            } else {
                // Use the most recent list or create a "Quick List"
                allLists.firstOrNull()?.id ?: repository.addList("Quick List")
            }
            
            parseAndAddItem(targetListId, itemName)
        }
    }

    private fun guessCategory(itemName: String): String {
        val lowerItem = itemName.lowercase()
        return when {
            listOf("milk", "cheese", "yogurt", "butter", "cream").any { lowerItem.contains(it) } -> "Dairy"
            listOf("apple", "banana", "carrot", "tomato", "potato", "onion", "lettuce", "fruit", "veg", "spinach", "broccoli").any { lowerItem.contains(it) } -> "Produce"
            listOf("bread", "bagel", "croissant", "bun", "muffin").any { lowerItem.contains(it) } -> "Bakery"
            listOf("chicken", "beef", "pork", "fish", "meat", "salmon", "bacon").any { lowerItem.contains(it) } -> "Meat & Seafood"
            listOf("water", "juice", "soda", "coffee", "tea", "coke", "wine", "beer").any { lowerItem.contains(it) } -> "Beverages"
            listOf("chips", "cookie", "candy", "chocolate", "snack").any { lowerItem.contains(it) } -> "Snacks"
            listOf("soap", "shampoo", "toothpaste", "paper", "detergent", "cleaner").any { lowerItem.contains(it) } -> "Household"
            else -> ""
        }
    }

    // ── Barcode Scanner ────────────────────────────────────────────────

    fun onOpenScanner(listId: String) {
        uiState = uiState.copy(isScannerVisible = true, scannerListId = listId, scannerMessage = "")
    }

    fun onCloseScanner() {
        uiState = uiState.copy(isScannerVisible = false, scannerListId = null, scannerMessage = "")
    }

    fun onBarcodeScanned(barcode: String) {
        val listId = uiState.scannerListId ?: return
        
        if (!premiumManager.canScanBarcode()) {
            uiState = uiState.copy(isScannerVisible = false, isPremiumPaywallVisible = true)
            return
        }
        
        uiState = uiState.copy(scannerMessage = "Looking up barcode...")

        viewModelScope.launch {
            premiumManager.incrementBarcodeScan()
            val result = com.nikeshparihar.smartcart.data.remote.OpenFoodFactsApi.lookupBarcode(barcode)
            if (result != null) {
                val category = result.category.ifBlank { guessCategory(result.name) }
                
                // Check if the item already exists in the list
                val existingItem = shoppingLists.value.find { it.id == listId }?.items?.find {
                    it.name.equals(result.name, ignoreCase = true)
                }

                if (existingItem != null) {
                    val newQty = (existingItem.quantity ?: 1) + 1
                    repository.updateItem(existingItem.id, listId, existingItem.name, newQty, existingItem.category, existingItem.isPurchased)
                } else {
                    repository.addItem(listId, result.name, 1, category)
                }
                
                repository.sync()
                uiState = uiState.copy(
                    isScannerVisible = false,
                    scannerListId = null,
                    scannerMessage = ""
                )
            } else {
                // Product not found — let user type manually
                uiState = uiState.copy(
                    isScannerVisible = false,
                    scannerMessage = "",
                    operatingListId = listId,
                    isItemDialogVisible = true,
                    draftName = "Barcode: $barcode",
                    draftQuantity = "1",
                    draftCategory = ""
                )
            }
        }
    }

    fun onClosePaywall() {
        uiState = uiState.copy(isPremiumPaywallVisible = false)
    }
}
