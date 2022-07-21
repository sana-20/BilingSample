package com.example.bilingsample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bilingsample.BillingViewModel
import com.example.bilingsample.Constants
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
            viewModel.buy(Constants.PREMIUM_SUB, Constants.PREMIUM_MONTHLY_PLAN, false)
        }

        binding.button2.setOnClickListener {
            viewModel.buy(Constants.BASIC_SUB, Constants.PREMIUM_YEARLY_PLAN, false)
        }

        binding.button3.setOnClickListener {
            viewModel.buy(Constants.BASIC_SUB, Constants.PREMIUM_MONTHLY_PLAN, true)
        }

    }
}