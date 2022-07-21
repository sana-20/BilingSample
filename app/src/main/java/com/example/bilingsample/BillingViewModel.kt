package com.example.bilingsample

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.example.bilingsample.Constants.TAG

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val productDetails = (application as BillingApp).billingClientWrapper.productDetails

    val buyEvent = SingleLiveEvent<BillingFlowParams>()

    private val purchases =  (application as BillingApp).billingClientWrapper.purchases

    private fun billingFlowParamsBuilder(productDetails: ProductDetails, offerToken: String):
            BillingFlowParams {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        ).build()
    }


    private fun upDowngradeBillingFlowParamsBuilder(
        productDetails: ProductDetails, offerToken: String, oldToken: String
    ): BillingFlowParams {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        ).setSubscriptionUpdateParams(
            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(oldToken)
                .setReplaceProrationMode(BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_FULL_PRICE)
                .build()
        ).build()
    }


    private fun leastPricedOfferToken(
        offerDetails: List<ProductDetails.SubscriptionOfferDetails>
    ): String {
        var offerToken = String()
        var leastPricedOffer: ProductDetails.SubscriptionOfferDetails
        var lowestPrice = Int.MAX_VALUE

        if (offerDetails.isNotEmpty()) {
            for (offer in offerDetails) {
                for (price in offer.pricingPhases.pricingPhaseList) {
                    if (price.priceAmountMicros < lowestPrice) {
                        lowestPrice = price.priceAmountMicros.toInt()
                        leastPricedOffer = offer
                        offerToken = leastPricedOffer.offerToken
                    }
                }
            }
        }
        return offerToken
    }


    private fun retrieveEligibleOffers(
        offerDetails: MutableList<ProductDetails.SubscriptionOfferDetails>, tag: String
    ):
            List<ProductDetails.SubscriptionOfferDetails> {
        val eligibleOffers = emptyList<ProductDetails.SubscriptionOfferDetails>().toMutableList()
        offerDetails.forEach { offerDetail ->
            if (offerDetail.offerTags.contains(tag)) {
                eligibleOffers.add(offerDetail)
            }
        }
        return eligibleOffers
    }


    fun buy(product: String, tag: String, upDowngrade: Boolean) {
        val productDetails = productDetails.value?.get(product) ?: run {
            Log.e(TAG, "Could not find ProductDetails to make purchase.")
            return
        }

        val offers =
            productDetails.subscriptionOfferDetails?.let {
                retrieveEligibleOffers(
                    offerDetails = it,
                    tag = tag
                )
            }

        val offerToken = offers?.let { leastPricedOfferToken(it) }.toString()

        var oldToken = ""

        if (upDowngrade) {
            for (purchase in purchases.value) {
                oldToken = purchase.purchaseToken
            }

            val billingParams = upDowngradeBillingFlowParamsBuilder(
                productDetails = productDetails,
                offerToken = offerToken,
                oldToken = oldToken
            )

            buyEvent.postValue(billingParams)
        } else {
            val billingParams = billingFlowParamsBuilder(
                productDetails = productDetails,
                offerToken = offerToken
            )

            buyEvent.postValue(billingParams)
        }
    }

}
