package com.example.bilingsample.biling

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.example.bilingsample.Constants.LIST_OF_PRODUCTS
import com.example.bilingsample.Constants.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class BillingClientWrapper(
    context: Context
) : PurchasesUpdatedListener, ProductDetailsResponseListener {

    private val _productDetails = MutableLiveData<Map<String, ProductDetails>>()
    val productDetails : LiveData<Map<String, ProductDetails>> = _productDetails

    private val _connection = MutableLiveData<Boolean>()
    val connection: LiveData<Boolean> = _connection


    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing response OK")
                    _connection.postValue(true)
                    //queryPurchases()
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

    suspend fun query2() {
        val params = QueryProductDetailsParams.newBuilder()
        val productList = mutableListOf<QueryProductDetailsParams.Product>()
        for (product in LIST_OF_PRODUCTS) {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.setProductList(productList).build())
        }

        Log.d(TAG, "결과: $productDetailsResult")
    }


    fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready")
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                //_purchases.value = purchaseList.ifEmpty { emptyList() }
                Log.d(TAG, "구입 내역 리스트 성공")
            } else {
                Log.d(TAG, "구입 내역 리스트 실패")
                Log.e(TAG, billingResult.debugMessage)
            }
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
                _productDetails.postValue(newMap)
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
            //_purchases.value = purchases

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
                        //_isNewPurchaseAcknowledged.value = true
                    }
                }
            }
        }
    }

    fun terminateBillingConnection() {
        billingClient.endConnection()
    }




}