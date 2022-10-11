package com.example.szakchat.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.szakchat.MainActivity
import com.example.szakchat.R
import com.example.szakchat.databinding.FragmentExchangeBinding
import com.example.szakchat.viewModel.ChatViewModel
import com.example.szakchat.viewModel.MySecurityManager

class ExchangeFragment : Fragment() {

    private var _binding: FragmentExchangeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentExchangeBinding.inflate(inflater, container, false)

        val security = viewModel.security

        security.randomBytes.observe(viewLifecycleOwner){
            it?: return@observe
            when(it.state){
                MySecurityManager.MSG -> {
                    val activity = requireActivity() as MainActivity
                    activity.showSnack(it.msg)
                }
                MySecurityManager.START -> {
                    binding.createKeyProgress.visibility = View.VISIBLE
                }
                MySecurityManager.END -> {
                    binding.createKeyProgress.visibility = View.GONE
                    findNavController().navigate(R.id.navigate_to_qr)
                    security.clearMessage()
                }
            }
        }

        binding.btnKeyCreate.setOnClickListener {
            security.getBytes(1024)
        }

        binding.btnKeyRead.setOnClickListener {
            findNavController().navigate(R.id.navigate_to_camera)
        }

        return binding.root
    }


}