package com.example.bilingsample

object Constants {

    const val TAG = "BillingClient"

    //Product IDs
    const val BASIC_SUB = "basic_subscription"
    const val PREMIUM_SUB = "premium_subscription"

    val LIST_OF_PRODUCTS = listOf(BASIC_SUB, PREMIUM_SUB)

    //Tags
    const val BASIC_MONTHLY_PLAN = "basicmonthly"
    const val BASIC_YEARLY_PLAN = "basicyearly"
    const val PREMIUM_MONTHLY_PLAN = "premiummonthly"
    const val PREMIUM_YEARLY_PLAN = "premiumyearly"

    const val PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL
            = "https://play.google.com/store/account/subscriptions?product=%s&package=%s"

}