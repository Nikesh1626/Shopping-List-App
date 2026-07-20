package com.nikeshparihar.smartcart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.activity.viewModels
import com.nikeshparihar.smartcart.ui.AuthScreen
import com.nikeshparihar.smartcart.ui.theme.ShoppingListTheme
import com.nikeshparihar.smartcart.viewmodel.AuthViewModel
import com.nikeshparihar.smartcart.viewmodel.ShoppingListViewModel
import android.content.Intent

class MainActivity : ComponentActivity() {
    private val shoppingListViewModel: ShoppingListViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAssistantIntent(intent)
        enableEdgeToEdge()
        setContent {
            ShoppingListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val user = authViewModel.currentUser
                    if (user != null) {
                        // User is authenticated – show the app
                        ShoppingListApp(
                            viewModel = shoppingListViewModel,
                            authViewModel = authViewModel
                        )
                    } else {
                        // No user – show Auth screen
                        AuthScreen(authViewModel = authViewModel)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAssistantIntent(intent)
    }

    private fun handleAssistantIntent(intent: Intent?) {
        if (intent == null) return
        val itemName = intent.getStringExtra("item_name")
        val listName = intent.getStringExtra("list_name")
        
        if (!itemName.isNullOrBlank()) {
            shoppingListViewModel.handleAddFromAssistant(itemName, listName)
            // Clear the extras so they don't trigger again on rotation/re-creation
            intent.removeExtra("item_name")
            intent.removeExtra("list_name")
        }
    }
}
