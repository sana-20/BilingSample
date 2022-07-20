package com.example.bilingsample.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.billingclient.api.ProductDetails
import com.example.bilingsample.BillingViewModel
import com.example.bilingsample.Constants
import com.example.bilingsample.databinding.FragmentBasicBinding
import com.example.bilingsample.databinding.FragmentPremiumBinding

class PremiumFragment : Fragment() {

    private lateinit var binding: FragmentPremiumBinding

    private val viewModel: BillingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPremiumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            viewModel.buy( Constants.PREMIUM_SUB, false, requireActivity())
        }

        binding.button2.setOnClickListener {
            viewModel.buy( Constants.PREMIUM_SUB, true, requireActivity())
        }
    }
}