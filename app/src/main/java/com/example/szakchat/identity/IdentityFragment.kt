package com.example.szakchat.identity

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.szakchat.viewModel.ChatViewModel
import com.example.szakchat.R
import com.example.szakchat.databinding.FragmentIdentityBinding
import com.example.szakchat.extensions.isEmpty

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class IdentityFragment : Fragment() {

    private var _binding: FragmentIdentityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentIdentityBinding.inflate(inflater, container, false)
        binding.submitName.setOnClickListener {
            if(binding.nameField.isEmpty())
                return@setOnClickListener
            viewModel.networking.setSelfId(
                binding.nameField.text.toString(),
                requireActivity().getPreferences(Context.MODE_PRIVATE),
            )
            if(!binding.ipField.isEmpty())
                viewModel.networking.ip = binding.ipField.text.toString()

            findNavController().navigate(R.id.action_self_to_first)
        }
        binding.ipField.setText(viewModel.networking.ip)
        return binding.root
    }
}