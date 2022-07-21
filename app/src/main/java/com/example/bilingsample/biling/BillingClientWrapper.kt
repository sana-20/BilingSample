package com.example.bilingsample.biling

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.example.bilingsample.Constants.LIST_OF_PRODUCTS
import com.example.bilingsample.Constants.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingClientWrapper(
    private val context: Context
) : DefaultLifecycleObserver, PurchasesUpdatedListener, ProductDetailsResponseListener {

    val productDetails = MutableLiveData<Map<String, ProductDetails>>()

    private val _purchases = MutableStateFlow<List<Purchase>>(listOf())
    val purchases = _purchases.asStateFlow()

    private val _isNewPurchaseAcknowledged = MutableStateFlow(value = false)
    val isNewPurchaseAcknowledged = _isNewPurchaseAcknowledged.asStateFlow()

    private lateinit var billingClient: BillingClient

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        if (!billingClient.isReady) {
            startBillingConnection()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing response OK")
                    queryPurchases()
                    queryProductDetails()
                } else {
                    Log.i(TAG, billingResult.debugMessage)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.i(TAG, "Billing connection disconnected")
                startBillingConnection()
            }
        })
    }


    fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready")
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchaseList)
            } else {
                Log.e(TAG, billingResult.debugMessage)
            }
        }
    }

    private fun processPurchases(purchaseList: List<Purchase>) {
        CoroutineScope(Dispatchers.IO).launch {
            _purchases.emit(purchaseList)
        }
    }

    fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
        val productList = mutableListOf<QueryProductDetailsParams.Product>()
        for (product in LIST_OF_PRODUCTS) {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            params.setProductList(productList).let { productDetailParams ->
                billingClient.queryProductDetailsAsync(productDetailParams.build(), this)
            }
        }
    }


    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: MutableList<ProductDetails>
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                var newMap = emptyMap<String, ProductDetails>()
                if (productDetailsList.isEmpty()) {
                    Log.e(
                        TAG,
                        "onProductDetailsResponse: " +
                                "Found null or empty ProductDetails. " +
                                "Check to see if the Products you requested are correctly " +
                                "published in the Google Play Console."
                    )
                } else {
                    newMap = productDetailsList.associateBy { it.productId }
                }
                productDetails.postValue(newMap)
            }
            else -> {
                Log.d(
                    TAG,
                    "onProductDetailsResponse: ${billingResult.responseCode} ${billingResult.debugMessage}"
                )
            }
        }

    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams) {
        if (!billingClient.isReady) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready")
        }
        billingClient.launchBillingFlow(activity, params)
    }


    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
            && !purchases.isNullOrEmpty()
        ) {
            processPurchases(purchases)

            for (purchase in purchases) {
                acknowledgePurchases(purchase)
            }

        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User has cancelled")
        } else {
            Log.d(TAG, billingResult.debugMessage)
        }
    }

    private fun acknowledgePurchases(purchase: Purchase?) {
        purchase?.let {
            if (!it.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(params) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        _isNewPurchaseAcknowledged.value = true
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BillingClientWrapper? = null

        fun getInstance(applicationContext: Context): BillingClientWrapper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingClientWrapper(applicationContext).also { INSTANCE = it }
            }
    }

}