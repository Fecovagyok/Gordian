package com.example.szakchat.contacts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.szakchat.databinding.AddContactBinding
import com.example.szakchat.extensions.isBadText
import com.example.szakchat.viewModel.ChatViewModel

class AddContactDialog : DialogFragment() {

    private val viewModel: ChatViewModel by activityViewModels()

    private lateinit var binding: AddContactBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AddContactBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.contactAddOk.setOnClickListener {
            val text = binding.contactField.text.toString().trim()
            if(text.isBadText())
                return@setOnClickListener
            val contact = Contact(
                uniqueId = text,
            )
            viewModel.insertContact(contact)
            dismiss()
        }
    }
}