package com.example.szakchat.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.szakchat.databinding.ConfirmExchangeLayoutBinding

class ConfirmExchangeFragment : Fragment() {

    private var _binding: ConfirmExchangeLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ConfirmExchangeLayoutBinding.inflate(inflater, container, false)

        // Start pairing over server

        return binding.root
    }

}