package com.example.bilingsample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilingsample.biling.BillingClientWrapper
import com.example.bilingsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var billingClient: BillingClientWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        billingClient = BillingClientWrapper(this)
        billingClient.startBillingConnection()

        billingClient.productDetails.observe(this) {
            Log.d("로그", it.toString())

//            binding.recyclerView.apply {
//                adapter = ProductAdapter(it)
//                layoutManager = LinearLayoutManager(this@MainActivity)
//            }
        }


    }

}