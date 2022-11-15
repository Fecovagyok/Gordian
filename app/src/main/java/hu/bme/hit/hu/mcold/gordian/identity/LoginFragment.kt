package hu.bme.hit.hu.mcold.gordian.identity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import hu.bme.gordian.hu.mcold.gordian.R
import hu.bme.gordian.hu.mcold.gordian.databinding.FragmentIdentityBinding
import hu.bme.hit.hu.mcold.gordian.MainActivity
import hu.bme.hit.hu.mcold.gordian.common.isEmpty
import hu.bme.hit.hu.mcold.gordian.common.moreThan
import hu.bme.hit.hu.mcold.gordian.common.onError
import hu.bme.hit.hu.mcold.gordian.common.reSetupActionBar
import hu.bme.hit.hu.mcold.gordian.exceptions.AlreadyRunning
import hu.bme.hit.hu.mcold.gordian.viewModel.ChatViewModel
import hu.bme.hit.hu.mcold.gordian.viewModel.NetworkManager

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
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.usernameField.addTextChangedListener {
            binding.usernameLayout.isErrorEnabled = false
        }
        binding.passwordField.addTextChangedListener {
            binding.passwordLayout.isErrorEnabled = false
        }
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
                    viewModel.networking.checkPollingSync()
                    // Appbar fix
                    reSetupActionBar(a, R.id.FirstFragment) { controller ->
                        controller.navigate(R.id.action_login_to_first)
                    }
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
                if(!checkInputs())
                    return@setOnClickListener
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
    }

    private fun checkInputs(): Boolean{
        if (binding.usernameField.isEmpty()) {
            binding.usernameLayout.onError(getString(R.string.username_cannot_empty))
            return false
        }
        if(binding.passwordField.isEmpty()) {
            binding.passwordLayout.onError(getString(R.string.password_cannot_empty))
            return false
        }
        if (binding.usernameField.moreThan(MAX_CHARS)) {
            binding.usernameLayout.onError(maxCharError)
            return false
        }
        if(binding.passwordField.moreThan(MAX_CHARS)) {
            binding.passwordLayout.onError(maxCharError)
            return false
        }
        return true
    }

    private val maxCharError by lazy(LazyThreadSafetyMode.NONE){
        getString(R.string.max_characters_first_half) +
                MAX_CHARS.toString() + getString(R.string.max_characters_second_half)
    }
}