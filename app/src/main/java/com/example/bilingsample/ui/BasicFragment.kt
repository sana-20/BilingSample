package com.example.bilingsample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bilingsample.BillingViewModel
import com.example.bilingsample.Constants.BASIC_MONTHLY_PLAN
import com.example.bilingsample.Constants.BASIC_SUB
import com.example.bilingsample.Constants.BASIC_YEARLY_PLAN
import com.example.bilingsample.databinding.FragmentBasicBinding

class BasicFragment : Fragment() {

    private lateinit var binding: FragmentBasicBinding

    private val viewModel: BillingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBasicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            viewModel.buy(BASIC_SUB, BASIC_MONTHLY_PLAN, false)
        }

        binding.button2.setOnClickListener {
            viewModel.buy(BASIC_SUB, BASIC_YEARLY_PLAN, false)
        }

        binding.button3.setOnClickListener {
            viewModel.buy(BASIC_SUB, BASIC_MONTHLY_PLAN, true)
        }

    }

}