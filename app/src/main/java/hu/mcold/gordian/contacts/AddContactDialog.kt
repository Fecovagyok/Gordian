package hu.mcold.gordian.contacts

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import hu.mcold.gordian.R
import hu.mcold.gordian.databinding.AddContactBinding
import hu.mcold.gordian.common.isBadText
import hu.mcold.gordian.viewModel.ChatViewModel

class AddContactDialog : DialogFragment() {

    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val binding = AddContactBinding.inflate(layoutInflater)
        return AlertDialog.Builder(context)
            .setTitle(R.string.add_contact)
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { _, _ ->
                val text = binding.addContactInput.text.toString().trim()

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