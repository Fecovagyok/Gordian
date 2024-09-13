package hu.mcold.gordian.messages

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import haart.bme.gordian.hu.mcold.gordian.R
import haart.bme.gordian.hu.mcold.gordian.databinding.FragmentMessageBinding
import haart.bme.gordian.hu.mcold.gordian.databinding.SecretExpiredLayoutBinding
import haart.bme.hit.hu.mcold.gordian.common.isBadText
import haart.bme.hit.hu.mcold.gordian.common.scrollToTheEnd
import haart.bme.hit.hu.mcold.gordian.viewModel.ChatViewModel


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MessagesFragment : Fragment(), MessageAdapter.Listener, MenuProvider {

    private var _binding: FragmentMessageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()
    private lateinit var adapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        if(viewModel.currentContact!!.keys == null){
            initOverlay(inflater)
        }
        return binding.root

    }

    private fun initOverlay(inflater: LayoutInflater){
        val overlayBinding = SecretExpiredLayoutBinding.inflate(inflater, binding.root, false)
        overlayBinding.exchangeKeyOverlayBtn.setOnClickListener {
            findNavController().navigate(R.id.navigate_to_exchange)
        }
        binding.root.addView(overlayBinding.root)
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

    private fun MessageAdapter.scrollToTheEnd(size: Int){
        subscribed = {
            if(adapter.itemCount == size){
                binding.messagesView.scrollToPosition(size-1)
                adapter.subscribed = null
            }
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
            adapter.scrollToTheEnd(messages.size)
        }

        registerForContextMenu(binding.messagesView)

        binding.buttonSecond.setOnClickListener {
            if (viewModel.currentContact == null)
                return@setOnClickListener
            val text = binding.msgField.text.toString().trim()
            if(text.isBadText()){
                binding.messagesView.scrollToTheEnd()
                binding.msgField.error = getString(R.string.empty_msg_not_sent)
                return@setOnClickListener
            }

            val contact = viewModel.currentContact!!
            val message = Message(
                owner = viewModel.networking.self!!,
                contact = contact,
                text = text,
                incoming = false,
                date = System.currentTimeMillis(),
                sent = false,
            )

            viewModel.networking.send(message)
            binding.msgField.text.clear()
        }
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        binding.msgField.addTextChangedListener {
            binding.msgField.error = null
        }
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