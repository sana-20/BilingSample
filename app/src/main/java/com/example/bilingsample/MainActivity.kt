package com.example.bilingsample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.bilingsample.Constants.TAG
import com.example.bilingsample.biling.BillingClientWrapper
import com.example.bilingsample.databinding.ActivityMainBinding
import com.example.bilingsample.ui.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var billingViewModel: BillingViewModel

    private lateinit var billingClientWrapper: BillingClientWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingViewModel = ViewModelProvider(this)[BillingViewModel::class.java]
        billingClientWrapper = (application as BillingApp).billingClientWrapper
        lifecycle.addObserver(billingClientWrapper)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
            TabLayoutMediator(tabs, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = "basic"
                    1 -> tab.text = "premium"
                    2 -> tab.text = "settings"
                }
            }.attach()
        }

        billingViewModel.buyEvent.observe(this) {
            billingClientWrapper.launchBillingFlow(this, it)
        }
    }
}