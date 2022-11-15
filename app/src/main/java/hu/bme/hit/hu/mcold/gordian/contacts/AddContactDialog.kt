package hu.bme.hit.hu.mcold.gordian.contacts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import hu.bme.gordian.hu.mcold.gordian.R
import hu.bme.gordian.hu.mcold.gordian.databinding.AddContactBinding
import hu.bme.hit.hu.mcold.gordian.common.isBadText
import hu.bme.hit.hu.mcold.gordian.viewModel.ChatViewModel

class AddContactDialog : DialogFragment() {

    private val viewModel: ChatViewModel by activityViewModels()

    private lateinit var binding: AddContactBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddContactBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val input = EditText(context)
        return AlertDialog.Builder(context)
            .setTitle(R.string.add_contact)
            .setView(input)
            .setPositiveButton(R.string.ok) { _, _ ->
                val text = binding.contactField.text.toString().trim()
                if(text.isBadText())
                    return@setPositiveButton
                createContact(text)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    private fun createContact(name: String) {
        val contact = Contact(
            owner = viewModel.networking.self!!,
            name = name,
        )
        viewModel.insertContact(contact)
    }
}