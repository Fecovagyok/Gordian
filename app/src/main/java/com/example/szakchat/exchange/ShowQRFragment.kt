package com.example.szakchat.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.szakchat.databinding.FragmentExchangeBinding
import com.example.szakchat.databinding.FragmentQrShowBinding
import com.example.szakchat.viewModel.ChatViewModel

class ShowQRFragment : Fragment() {

    private var _binding: FragmentQrShowBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrShowBinding.inflate(inflater, container, false)
        val bitmap = MyQR.createQR(viewModel.security.secureBytes!!)
        bitmap?: return binding.root
        binding.qrImage.setImageBitmap(bitmap)
        return binding.root
    }
}