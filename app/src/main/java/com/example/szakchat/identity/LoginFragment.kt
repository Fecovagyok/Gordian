package com.example.szakchat.identity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.szakchat.R
import com.example.szakchat.databinding.FragmentIdentityBinding
import com.example.szakchat.extensions.isEmpty
import com.example.szakchat.viewModel.ChatViewModel

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentIdentityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentIdentityBinding.inflate(inflater, container, false)
        binding.loginButton.setOnClickListener {
            if(binding.usernameField.isEmpty() || binding.passwordField.isEmpty())
                return@setOnClickListener

            findNavController().navigate(R.id.action_login_to_first)
        }
        viewModel.networking.self?.let {
            binding.nameField.setText(it)
        }
        return binding.root
    }
}