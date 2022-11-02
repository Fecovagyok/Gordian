package com.example.szakchat.messages

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.szakchat.R
import com.example.szakchat.databinding.FragmentSecondBinding
import com.example.szakchat.databinding.SecretExpiredLayoutBinding
import com.example.szakchat.extensions.isBadText
import com.example.szakchat.extensions.scrollToTheEnd
import com.example.szakchat.viewModel.ChatViewModel

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MessagesFragment : Fragment(), MessageAdapter.Listener, MenuProvider {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()
    private lateinit var adapter: MessageAdapter
    private var overlayBinding: SecretExpiredLayoutBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        if(viewModel.currentContact!!.keys == null){
            overlayBinding = SecretExpiredLayoutBinding.inflate(inflater, binding.root, false)
            binding.root.addView(overlayBinding!!.root)
        }
        return binding.root

    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = requireActivity().menuInflater
        Log.d("FECO", "OnCreaateContextMenu")
        inflater.inflate(R.menu.menu_context_unsent, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.d("FECO", "OnItemSelected: ${item.menuInfo}")
        return when(item.itemId){
            R.id.context_delete -> {
                adapter.longClickedItem?.let {
                    viewModel.removeMessage(it)
                }
                Log.d("FECO", "DELETEE")
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        val contact = viewModel.currentContact
        activity.supportActionBar?.title = contact?.name ?: "Unknown"
        activity.supportActionBar

        val manager = binding.messagesView.layoutManager as LinearLayoutManager
        manager.stackFromEnd = true

        adapter = MessageAdapter(this)
        binding.messagesView.adapter = adapter
        viewModel.currentMessages?.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            val size = messages.size
            adapter.subscribed = {
                if(adapter.itemCount == size){
                    binding.messagesView.scrollToPosition(messages.size - 1)
                    adapter.subscribed = null
                }
            }
        }

        registerForContextMenu(binding.messagesView)

        binding.buttonSecond.setOnClickListener {
            if (viewModel.currentContact == null)
                return@setOnClickListener
            val text = binding.msgField.text.toString().trim()
            if(text.isBadText()){
                binding.messagesView.scrollToTheEnd()
                Log.d("FECO", "BadText, Textbox: $text")
                return@setOnClickListener
            }

            val contact = viewModel.currentContact!!
            val message = Message(
                owner = viewModel.networking.self!!,
                contact = contact,
                text = text,
                incoming = false,
                sent = false,
            )

            viewModel.networking.send(message)
            binding.msgField.text.clear()
        }
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.message_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.action_exchange_secret -> {
                findNavController().navigate(R.id.navigate_to_exchange)
                true
            }
            else -> false
        }
    }
}