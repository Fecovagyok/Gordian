package com.example.szakchat.messages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.szakchat.viewModel.ChatViewModel
import com.example.szakchat.databinding.FragmentSecondBinding
import com.example.szakchat.extensions.isBadText
import com.example.szakchat.network.ChatSocket

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MessagesFragment : Fragment(), MessageAdapter.Listener {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        val contact = viewModel.currentContact
        activity.supportActionBar?.title = contact?.name ?: "Unknown"

        val adapter = MessageAdapter(this)
        binding.messagesView.adapter = adapter
        viewModel.currentMessages?.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
        }

        binding.buttonSecond.setOnClickListener {
            if (viewModel.currentContact == null)
                return@setOnClickListener
            val text = binding.msgField.text.toString().trim()
            if(text.isBadText())
                return@setOnClickListener

            val contact = viewModel.currentContact!!
            val message = Message(
                contact = contact,
                text = text,
                incoming = false,
                sent = false,
            )

            viewModel.networking.send(message)
            binding.msgField.text.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}