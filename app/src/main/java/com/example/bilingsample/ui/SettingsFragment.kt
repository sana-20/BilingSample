package com.example.bilingsample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bilingsample.BillingViewModel
import com.example.bilingsample.databinding.FragmentBasicBinding
import com.example.bilingsample.databinding.FragmentSettingsBinding

class SettingsFragment: Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val viewModel: BillingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
}