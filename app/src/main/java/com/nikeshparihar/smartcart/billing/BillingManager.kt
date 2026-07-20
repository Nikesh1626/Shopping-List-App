package com.nikeshparihar.smartcart.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(private val context: Context, private val applicationScope: CoroutineScope) : PurchasesUpdatedListener {

    private val playConsoleProductId = "premium_monthly_sub"
    
    // Debug mock mode: MUST be false for production builds
    private val isDebugMockMode = false

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Query for existing purchases to restore true state
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Connection to Play Store lost; we could retry here.
            }
        })
    }

    private fun queryPurchases() {
        if (isDebugMockMode) return // Not querying actual subscriptions in debug
        
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchases.any { purchase ->
                    purchase.products.contains(playConsoleProductId) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _isPremium.value = hasActiveSub
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        if (isDebugMockMode) {
            // Bypass google play billing and directly grant premium for local testing
            _isPremium.value = true
            return
        }
        
        if (!billingClient.isReady) {
            _errorMessage.value = "Billing Client not ready."
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(playConsoleProductId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList.first()
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                
                if (offerToken != null) {
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    billingClient.launchBillingFlow(activity, billingFlowParams)
                } else {
                    _errorMessage.value = "No valid subscription offers found."
                }
            } else {
                _errorMessage.value = "Item Unavailable or Developer Error. Please setup Play Console."
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("BillingManager", "User canceled purchase flow")
        } else {
            Log.e("BillingManager", "Billing Error: ${billingResult.debugMessage}")
            _errorMessage.value = "Billing Error: ${billingResult.debugMessage}"
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            _isPremium.value = true

            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                applicationScope.launch(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d("BillingManager", "Purchase acknowledged")
                        }
                    }
                }
            }
        }
    }
}
