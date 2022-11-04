package com.example.szakchat.identity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.szakchat.MainActivity
import com.example.szakchat.R
import com.example.szakchat.databinding.FragmentIdentityBinding
import com.example.szakchat.exceptions.AlreadyRunning
import com.example.szakchat.extensions.isEmpty
import com.example.szakchat.extensions.moreThan
import com.example.szakchat.viewModel.ChatViewModel
import com.example.szakchat.viewModel.NetworkManager

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

    private fun prefs() = requireActivity().getPreferences(Context.MODE_PRIVATE)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentIdentityBinding.inflate(inflater, container, false)
        binding.usernameField.setText(prefs().getString(NetworkManager.NAME_KEY, null))
        viewModel.networking.authData.observe(viewLifecycleOwner) {
            val a = requireActivity() as MainActivity
            when(it.normal) {
                true -> {
                    viewModel.networking.setSelfCredentialsPermanently(
                        name = binding.usernameField.text.toString(),
                        pass = binding.passwordField.text.toString(),
                        prefs = prefs(),
                    )
                    // Appbar fix
                    val controller = findNavController()
                    controller.graph.setStartDestination(R.id.FirstFragment)
                    val configuration = AppBarConfiguration(controller.graph)
                    controller.navigate(R.id.action_login_to_first)
                    a.setupActionBarWithNavController(controller, configuration)
                }
                false -> {
                    a.showSnack(it.message)
                }
            }
            binding.loginProgress.visibility = View.GONE
        }
        binding.loginButton.setOnClickListener {
            val a = requireActivity() as MainActivity
            try {

                if (binding.usernameField.isEmpty() || binding.passwordField.isEmpty()) {
                    return@setOnClickListener
                }
                if (binding.usernameField.moreThan(50) || binding.passwordField.moreThan(50)) {
                    return@setOnClickListener
                }

                binding.loginProgress.visibility = View.VISIBLE
                viewModel.networking.loginRequest(
                    binding.usernameField.text.toString(),
                    binding.passwordField.text.toString(),
                )


            } catch (e: AlreadyRunning){
                a.showSnack(R.string.login_request_running)
            }
        }
        viewModel.networking.username?.let {
            binding.usernameField.setText(it)
        }
        return binding.root
    }
}