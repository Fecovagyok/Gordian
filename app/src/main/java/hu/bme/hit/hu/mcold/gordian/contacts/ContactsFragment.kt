package hu.bme.gordian.hu.mcold.gordian.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.szakchat.R
import com.example.szakchat.databinding.FragmentFirstBinding
import com.example.szakchat.viewModel.ChatViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ContactsFragment : Fragment(), ContactAdapter.ContactListener {

    companion object{
        const val DTAG = "FECO"
    }

    private var _binding: FragmentFirstBinding? = null

    private val viewModel: ChatViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContactAdapter(this)

        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            adapter.submitList(contacts)
        }
        binding.listContact.adapter = adapter

        binding.fab.setOnClickListener {
            val dialog = AddContactDialog()
            dialog.show(parentFragmentManager, "Add contact")
        }

        //binding.buttonFirst.setOnClickListener {
         //   findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        //}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(contact: Contact) {
        viewModel.currentContact = contact
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    override fun onLongClick() {
        // TODO("Not yet implemented")
    }
}