package com.nikeshparihar.smartcart

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.speech.RecognizerIntent
import com.nikeshparihar.smartcart.model.ShoppingItem
import com.nikeshparihar.smartcart.ui.BarcodeScannerScreen
import com.nikeshparihar.smartcart.ui.theme.CardOutline
import com.nikeshparihar.smartcart.ui.theme.CardWhite
import com.nikeshparihar.smartcart.ui.theme.DarkGreen
import com.nikeshparihar.smartcart.ui.theme.JakartaFontFamily
import com.nikeshparihar.smartcart.ui.theme.LightBgColour
import com.nikeshparihar.smartcart.ui.theme.LightGreen
import com.nikeshparihar.smartcart.ui.theme.MutedText
import com.nikeshparihar.smartcart.ui.theme.SoftSalmon
import com.nikeshparihar.smartcart.viewmodel.AppUiState
import com.nikeshparihar.smartcart.viewmodel.ShoppingListViewModel
import com.nikeshparihar.smartcart.viewmodel.AuthViewModel
import com.nikeshparihar.smartcart.model.ShoppingList

// ── Navigation ─────────────────────────────────────────────────────────────────

private sealed class Screen {
    object Lists : Screen()
    data class ListDetail(val listId: String) : Screen()
    object Profile : Screen()
}

// ── Bottom tabs ─────────────────────────────────────────────────────────────────

private data class BottomTab(val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab("Lists", Icons.Default.Home),
    BottomTab("Profile", Icons.Default.Person),
)

// ── Root composable ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListApp(viewModel: ShoppingListViewModel, authViewModel: AuthViewModel) {
    val uiState = viewModel.uiState
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    var currentScreen: Screen by remember { mutableStateOf(Screen.Lists) }

    LaunchedEffect(Unit) {
        viewModel.forceSync()
    }

    BackHandler(enabled = currentScreen is Screen.ListDetail) {
        currentScreen = Screen.Lists
    }

    val selectedTab = if (currentScreen is Screen.Profile) "Profile" else "Lists"

    Scaffold(
        containerColor = LightBgColour,
        floatingActionButton = {
            if (currentScreen == Screen.Lists) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::onAddListClick,
                    modifier = Modifier.height(72.dp),
                    shape = RoundedCornerShape(50),
                    contentColor = Color.White,
                    containerColor = SoftSalmon,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                        )
                    },
                    text = {
                        Text(
                            text = "Add List",
                            fontSize = 18.sp,
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 24.dp), // Floating pill padding
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = Color.White,
                    border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth().height(72.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomTabs.forEach { tab ->
                            val isSelected = selectedTab == tab.label
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .clickable {
                                        currentScreen = if (tab.label == "Profile") Screen.Profile else Screen.Lists
                                    }
                                    .background(if (isSelected) Color(0xFFFFDDE4) else Color.Transparent)
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) Color(0xFF333333) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    tab.label.uppercase(),
                                    fontSize = 11.sp,
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF333333) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (val screen = currentScreen) {
                is Screen.Lists -> ListsScreen(
                    shoppingLists = shoppingLists,
                    viewModel = viewModel,
                    onListClick = { listId -> currentScreen = Screen.ListDetail(listId) },
                    modifier = Modifier.fillMaxSize(),
                )
                is Screen.ListDetail -> {
                    val list = shoppingLists.find { it.id == screen.listId }
                    if (list != null) {
                        ListDetailScreen(
                            list = list,
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Lists },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        LaunchedEffect(Unit) { currentScreen = Screen.Lists }
                    }
                }
                is Screen.Profile -> ProfileScreen(
                    authViewModel = authViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        val isPremium by viewModel.isPremium.collectAsState()
        if (!isPremium) {
            PremiumBannerAd()
        }
    }
}

    // Full-screen Scanner Overlay
    if (uiState.isScannerVisible) {
        BarcodeScannerScreen(
            onBarcodeScanned = viewModel::onBarcodeScanned,
            onClose = viewModel::onCloseScanner,
            scannerMessage = uiState.scannerMessage
        )
    }

    // Dialogs are hoisted to root so they overlay any screen
    if (uiState.isListDialogVisible) {
        AddListDialog(
            draftName = uiState.draftListName,
            draftSharedEmail = uiState.draftSharedEmail,
            onNameChange = viewModel::onDraftListNameChange,
            onSharedEmailChange = viewModel::onDraftSharedEmailChange,
            onConfirm = viewModel::onConfirmListDialog,
            onCancel = viewModel::onCancelListDialog,
        )
    }
    if (uiState.isItemDialogVisible) {
        ItemDialog(
            isEditing = uiState.editingItem != null,
            draftName = uiState.draftName,
            draftQuantity = uiState.draftQuantity,
            draftCategory = uiState.draftCategory,
            onNameChange = viewModel::onDraftNameChange,
            onQuantityChange = viewModel::onDraftQuantityChange,
            onCategoryChange = viewModel::onDraftCategoryChange,
            onConfirm = viewModel::onConfirmItemDialog,
            onCancel = viewModel::onCancelItemDialog,
        )
    }

    if (uiState.isPremiumPaywallVisible) {
        val context = LocalContext.current
        PremiumPaywallScreen(
            onSubscribe = { 
                val activity = context as? android.app.Activity
                if (activity != null) {
                    viewModel.premiumManager.billingManager.launchBillingFlow(activity)
                }
                viewModel.onClosePaywall()
            },
            onDismiss = { viewModel.onClosePaywall() }
        )
    }
}

// ── Screen 1: Lists ─────────────────────────────────────────────────────────────

@Composable
private fun ListsScreen(
    shoppingLists: List<ShoppingList>,
    viewModel: ShoppingListViewModel,
    onListClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        val uiState = viewModel.uiState
        
        Text(
            "My Lists",
            fontSize = 42.sp,
            color = DarkGreen,
            fontWeight = FontWeight.Bold,
            fontFamily = JakartaFontFamily,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
        Text(
            text = "Predictive shopping suggestions",
            color = MutedText,
            fontSize = 14.sp,
            fontFamily = JakartaFontFamily,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
        )
        if (uiState.suggestions.isNotEmpty()) {
            SuggestionSection(
                suggestions = uiState.suggestions,
                onAddSuggestion = { suggestion ->
                    viewModel.handleAddFromAssistant(suggestion.itemName)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (shoppingLists.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = CardOutline,
                        modifier = Modifier.size(72.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No lists yet",
                        color = MutedText,
                        fontFamily = JakartaFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tap + to create your first list",
                        color = MutedText.copy(alpha = 0.7f),
                        fontFamily = JakartaFontFamily,
                        fontSize = 14.sp,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(shoppingLists) { list ->
                    ShoppingListCard(
                        list = list,
                        onClick = { onListClick(list.id) },
                        onEdit = { viewModel.onEditListClick(list) },
                        onDelete = { viewModel.onDeleteList(list.id) },
                    )
            }
        }
    }
}
}

@Composable
private fun SuggestionSection(
    suggestions: List<com.nikeshparihar.smartcart.util.PredictionResult>,
    onAddSuggestion: (com.nikeshparihar.smartcart.util.PredictionResult) -> Unit
) {
    Column {
        Text(
            "RECOMMENDED FOR YOU",
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            color = MutedText,
            fontWeight = FontWeight.Bold,
            fontFamily = JakartaFontFamily,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 24.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(
                    name = suggestion.itemName,
                    onClick = { onAddSuggestion(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    name: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardOutline.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = DarkGreen
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontSize = 14.sp,
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Medium,
                color = DarkGreen
            )
        }
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingList,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(36.dp),
        color = DarkGreen,
        shadowElevation = 8.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = list.name.take(1).uppercase(),
                    color = CardWhite,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JakartaFontFamily,
                    fontSize = 20.sp,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    fontSize = 32.sp,
                    color = CardWhite,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JakartaFontFamily,
                )
                Text(
                    text = "${list.items.size} product${if (list.items.size != 1) "s" else ""}",
                    fontSize = 13.sp,
                    color = CardWhite.copy(alpha = 0.8f),
                    fontFamily = JakartaFontFamily,
                )
            }
            Box {
                IconButton(
                    onClick = { isMenuOpen = true },
                    modifier = Modifier
                        .clip(CircleShape)

                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "List menu", tint = CardWhite)
                }
                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit / Share") },
                            onClick = {
                                isMenuOpen = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                isMenuOpen = false
                                onDelete()
                            },
                        )
                    }
            }
        }
    }
}

// ── Screen 2: List Detail ───────────────────────────────────────────────────────

@Composable
private fun ListDetailScreen(
    list: ShoppingList,
    viewModel: ShoppingListViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = data?.get(0) ?: ""
            viewModel.parseAndAddItem(list.id, spokenText)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(LightBgColour)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.clip(CircleShape).background(CardWhite)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkGreen)
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val uri = com.nikeshparihar.smartcart.util.PdfService.generatePdf(context, list)
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                            }
                        }
                    },
                    modifier = Modifier.clip(CircleShape).background(CardWhite)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = DarkGreen)
                }
            }

            // Header Area
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = list.name,
                    color = DarkGreen,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = JakartaFontFamily,
                )
                Text(
                    text = "${list.items.size} item${if (list.items.size != 1) "s" else ""}",
                    color = MutedText,
                    fontSize = 15.sp,
                    fontFamily = JakartaFontFamily,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            // Item List
            if (list.items.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No items yet.\nTap Add to get started.",
                        color = MutedText,
                        fontFamily = JakartaFontFamily,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                val activeItems = list.items.filter { !it.isPurchased }
                val purchasedItems = list.items.filter { it.isPurchased }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp), // space for FAB
                ) {
                    if (activeItems.isNotEmpty()) {
                        items(items = activeItems, key = { it.id }) { item ->
                            Box(modifier = Modifier.animateItem()) {
                                ShoppingListItem(
                                    item = item,
                                    onEdit = { viewModel.onEditItemClick(list.id, item) },
                                    onDelete = { viewModel.onDeleteItem(item) },
                                    onTogglePurchased = { viewModel.onToggleItemPurchased(item) },
                                )
                            }
                        }
                    }

                    if (purchasedItems.isNotEmpty()) {
                        item(key = "purchased_header") {
                            Text(
                                text = "Purchased Items",
                                color = DarkGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JakartaFontFamily,
                                modifier = Modifier
                                    .padding(top = 16.dp, bottom = 4.dp)
                                    .animateItem()
                            )
                        }
                        items(items = purchasedItems, key = { it.id }) { item ->
                            Box(modifier = Modifier.animateItem()) {
                                ShoppingListItem(
                                    item = item,
                                    onEdit = { viewModel.onEditItemClick(list.id, item) },
                                    onDelete = { viewModel.onDeleteItem(item) },
                                    onTogglePurchased = { viewModel.onToggleItemPurchased(item) },
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            ExpandableSpeedDialFab(
                onScanClick = { viewModel.onOpenScanner(list.id) },
                onMicClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak an item (e.g., '2 apples')")
                    }
                    try {
                        speechRecognizerLauncher.launch(intent)
                    } catch (e: Exception) {
                        // Ignore if no speech recognizer is available
                    }
                },
                onTypeClick = { viewModel.onAddItemClick(list.id) }
            )
        }
    }
}

@Composable
private fun ExpandableSpeedDialFab(
    onScanClick: () -> Unit,
    onMicClick: () -> Unit,
    onTypeClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ), label = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Scan Barcode", color = DarkGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = JakartaFontFamily, modifier = Modifier.padding(end = 12.dp))
                    androidx.compose.material3.SmallFloatingActionButton(onClick = { expanded = false; onScanClick() }, containerColor = CardWhite, contentColor = DarkGreen, shape = CircleShape) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Voice Assistant", color = DarkGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = JakartaFontFamily, modifier = Modifier.padding(end = 12.dp))
                    androidx.compose.material3.SmallFloatingActionButton(onClick = { expanded = false; onMicClick() }, containerColor = CardWhite, contentColor = DarkGreen, shape = CircleShape) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Type Manually", color = DarkGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = JakartaFontFamily, modifier = Modifier.padding(end = 12.dp))
                    androidx.compose.material3.SmallFloatingActionButton(onClick = { expanded = false; onTypeClick() }, containerColor = CardWhite, contentColor = DarkGreen, shape = CircleShape) {
                        Icon(Icons.Default.Edit, contentDescription = "Type")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = SoftSalmon,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Actions",
                modifier = Modifier.rotate(rotation).size(32.dp)
            )
        }
    }
}


// ── Screen 3: Profile ───────────────────────────────────────────────────────────

@Composable
private fun ProfileScreen(authViewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val user = authViewModel.currentUser
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Profile",
            fontSize = 42.sp,
            color = DarkGreen,
            fontWeight = FontWeight.Bold,
            fontFamily = JakartaFontFamily,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        )
        
        // Avatar Card
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = DarkGreen,
            modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.email?.take(1)?.uppercase() ?: "U",
                        color = CardWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JakartaFontFamily
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "User",
                        fontSize = 24.sp,
                        color = CardWhite,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JakartaFontFamily
                    )
                    Text(
                        text = user?.email ?: "Unknown Email",
                        fontSize = 14.sp,
                        color = CardWhite.copy(alpha = 0.8f),
                        fontFamily = JakartaFontFamily
                    )
                }
            }
        }

        // Settings Category: Account
        Text("ACCOUNT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MutedText, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp, start = 8.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardOutline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column {
                SettingsRow(Icons.Default.Settings, "App Preferences")
                androidx.compose.material3.HorizontalDivider(color = LightBgColour, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(Icons.Default.Notifications, "Notifications")
                androidx.compose.material3.HorizontalDivider(color = LightBgColour, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(Icons.Default.Palette, "Appearance")
            }
        }

        // Settings Category: Data
        Text("DATA & PRIVACY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MutedText, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp, start = 8.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardOutline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Column {
                SettingsRow(Icons.Default.CloudDownload, "Export Data")
                androidx.compose.material3.HorizontalDivider(color = LightBgColour, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(Icons.Default.PrivacyTip, "Privacy Policy")
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { authViewModel.signOut() },
            colors = ButtonDefaults.buttonColors(containerColor = SoftSalmon.copy(alpha = 0.1f), contentColor = SoftSalmon),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(
                text = "Log Out",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JakartaFontFamily
            )
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(LightBgColour), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 16.sp, color = DarkGreen, fontWeight = FontWeight.SemiBold, fontFamily = JakartaFontFamily, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MutedText)
    }
}


// ── Dialogs ─────────────────────────────────────────────────────────────────────

@Composable
private fun AddListDialog(
    draftName: String,
    draftSharedEmail: String,
    onNameChange: (String) -> Unit,
    onSharedEmailChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        shape = RoundedCornerShape(26.dp),
        containerColor = CardWhite,
        onDismissRequest = onCancel,
        confirmButton = {},
        title = {
            Text(
                if (draftName.isEmpty()) "New Shopping List" else "Edit / Share List",
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = JakartaFontFamily,
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    label = { Text("List Name") },
                    value = draftName,
                    onValueChange = onNameChange,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightGreen,
                        unfocusedContainerColor = LightGreen,
                        disabledContainerColor = LightGreen,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    label = { Text("Share with (Email)") },
                    placeholder = { Text("Optional", color = Color.Gray) },
                    value = draftSharedEmail,
                    onValueChange = onSharedEmailChange,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightGreen,
                        unfocusedContainerColor = LightGreen,
                        disabledContainerColor = LightGreen,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = SoftSalmon,
                        ),
                    ) {
                        Text(
                            "Create",
                            fontSize = 20.sp,
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun ItemDialog(
    isEditing: Boolean,
    draftName: String,
    draftQuantity: String,
    draftCategory: String,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        shape = RoundedCornerShape(26.dp),
        containerColor = CardWhite,
        onDismissRequest = onCancel,
        confirmButton = {},
        title = {
            Text(
                if (isEditing) "Edit Item" else "Add Item",
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = JakartaFontFamily,
            )
        },
        text = {
            Column {
                listOf(
                    Triple("Item Name", draftName, onNameChange),
                    Triple("Quantity", draftQuantity, onQuantityChange),
                    Triple("Category", draftCategory, onCategoryChange),
                ).forEach { (label, value, onChange) ->
                    OutlinedTextField(
                        label = { Text(label) },
                        value = value,
                        onValueChange = onChange,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LightGreen,
                            unfocusedContainerColor = LightGreen,
                            disabledContainerColor = LightGreen,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                DialogButtons(
                    confirmLabel = if (isEditing) "Save Changes" else "Add Item",
                    onConfirm = onConfirm,
                    onCancel = onCancel,
                )
            }
        },
    )
}

@Composable
private fun DialogButtons(
    confirmLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = SoftSalmon,
            ),
        ) { Text(confirmLabel) }
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = DarkGreen,
            ),
        ) { Text("Cancel") }
    }
}

// ── Item card ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePurchased: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.35f }, // More subtle swipe threshold
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTogglePurchased()
                    false // Return false so it smoothly snaps back instead of lingering
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                    false // Return false so the red box doesn't hang. The item moving out handles the disappearance!
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> DarkGreen.copy(alpha = 0.8f) // Purchased swipe color
                    SwipeToDismissBoxValue.EndToStart -> SoftSalmon // Delete swipe color
                    else -> Color.Transparent
                }
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> Icons.Default.Delete
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp)
                    .background(color, RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (direction != SwipeToDismissBoxValue.Settled) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            }
        },
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .alpha(if (item.isPurchased) 0.5f else 1f),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF0F2F0), // Clean light grey/green background as requested
                shadowElevation = 3.dp, // Premium soft shadow
                border = BorderStroke(0.5.dp, LightGreen.copy(alpha = 0.3f)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkGreen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.name.take(1).uppercase(),
                            color = CardWhite,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JakartaFontFamily,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.name,
                            fontSize = 18.sp,
                            color = DarkGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JakartaFontFamily,
                            textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                        )
                        Text(
                            "Qty: ${item.quantity ?: "N/A"}",
                            fontSize = 13.sp,
                            color = MutedText,
                            fontFamily = JakartaFontFamily,
                            textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                        )
                        Text(
                            "Category: ${item.category.ifBlank { "Uncategorized" }}",
                            fontSize = 13.sp,
                            color = MutedText,
                            fontFamily = JakartaFontFamily,
                            textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LightBgColour),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DarkGreen)
                    }
                }
            }
        }
    )
}

// ── Monetization UI ─────────────────────────────────────────────────────────────

@Composable
fun PremiumBannerAd(modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            com.google.android.gms.ads.AdView(context).apply {
                setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                // Official Test Ad Unit ID for Android Banners
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
            }
        }
    )
}

@Composable
private fun PremiumPaywallScreen(
    onSubscribe: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CardWhite
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("👑", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Go Premium", 
                    fontSize = 32.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = DarkGreen,
                    fontFamily = JakartaFontFamily
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "You've hit a free tier limit.\nUnlock unlimited list sharing, unlimited barcode scans, and remove all ads.", 
                    fontSize = 16.sp, 
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    fontFamily = JakartaFontFamily
                )
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Subscribe for \$4.99/mo", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = JakartaFontFamily)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Maybe Later", color = MutedText, fontSize = 16.sp, fontFamily = JakartaFontFamily)
                }
            }
        }
    }
}
