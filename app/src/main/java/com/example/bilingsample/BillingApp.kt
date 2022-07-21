package com.example.bilingsample

import android.app.Application
import com.example.bilingsample.biling.BillingClientWrapper

class BillingApp: Application() {

    val billingClientWrapper: BillingClientWrapper
        get() = BillingClientWrapper.getInstance(this)

}