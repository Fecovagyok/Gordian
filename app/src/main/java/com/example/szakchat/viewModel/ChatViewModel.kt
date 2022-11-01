package com.example.szakchat.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.szakchat.ChatApplication
import com.example.szakchat.contacts.Contact
import com.example.szakchat.contacts.ContactRepository
import com.example.szakchat.identity.UserID
import com.example.szakchat.messages.Message
import com.example.szakchat.messages.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel() : ViewModel() {

    private var _networking: NetworkManager? = null
    val networking get() = _networking!!
    val security = MySecurityManager(this)
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

    fun initNetwork(ip: String){
        _networking = NetworkManager(this, ip)
        networking.startPollStartJob()
    }

    fun getContacts(list: List<UserID>) = repository.getContacts(list)

    fun insertContact(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(contact)
    }
    fun insertMessage(message: Message) = viewModelScope.launch(Dispatchers.IO) {
        messageRepository.insert(message)
    }

    fun removeMessage(msg: Message) = viewModelScope.launch(Dispatchers.IO) {
        messageRepository.remove(msg)
    }
}