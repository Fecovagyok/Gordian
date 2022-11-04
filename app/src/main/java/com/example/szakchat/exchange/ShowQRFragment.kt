package com.example.szakchat.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.szakchat.R
import com.example.szakchat.contacts.Contact
import com.example.szakchat.databinding.FragmentQrShowBinding
import com.example.szakchat.viewModel.ChatViewModel

class ShowQRFragment : Fragment() {

    private var _binding: FragmentQrShowBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrShowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val bitmap = MyQR.createQR(viewModel.security.secureString!!)
        viewModel.networking.startHelloChannel()
        binding.btnCancelQr.setOnClickListener {
            viewModel.networking.cancelChannel()
            findNavController().popBackStack()
        }
        bitmap?: return
        binding.btnSuccessQr.setOnClickListener {
            secretExchangeSuccess()
        }
        binding.qrImage.setImageBitmap(bitmap)
    }

    private fun Contact.toContactWithKey(bytes: ByteArray) : Contact {
        val keys = viewModel.security.processQrDataAsShower(bytes)
        return Contact(
            id = id,
            owner = owner,
            uniqueId = uniqueId,
            name = name,
            keys = keys,
        )
    }

    private fun secretExchangeSuccess(){
        val bytes = viewModel.security.generatedLotsOfBytes!!
        val newContact = viewModel.currentContact!!.toContactWithKey(bytes)
        viewModel.currentContact = newContact
        viewModel.listenHello(newContact)
        findNavController().navigate(R.id.from_show_to_confirm)
    }
}