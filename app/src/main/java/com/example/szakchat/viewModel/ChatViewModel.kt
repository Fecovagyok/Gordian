package com.example.szakchat.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.szakchat.ChatApplication
import com.example.szakchat.contacts.Contact
import com.example.szakchat.contacts.ContactRepository
import com.example.szakchat.messages.Message
import com.example.szakchat.messages.MessageRepository
import com.example.szakchat.network.ChatSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    companion object {
        const val DEFAULT_IP = "89.133.85.78"
    }
    val networking = NetworkViewModel(this)
    var currentMessages: LiveData<List<Message>>? = null
        private set
    val messageRepository = MessageRepository()
    var currentContact: Contact? = null
    set(value) {
        field = value
        value?.let {
            currentMessages = messageRepository.getMessages(it)
        }
    }
    val repository = ContactRepository(
        ChatApplication.database.contactDao()
    )
    val contacts: LiveData<List<Contact>> = repository.getContacts()

    fun getContacts(list: List<String>) = repository.getContacts(list)

    fun insertContact(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }
    fun insertMessage(message: Message) = viewModelScope.launch {
        messageRepository.insert(message)
    }
    fun insertMessage(messages: List<Message>) = viewModelScope.launch {
        messageRepository.insert(messages)
    }
}