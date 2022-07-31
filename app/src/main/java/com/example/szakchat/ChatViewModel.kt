package com.example.szakchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.szakchat.contacts.Contact
import com.example.szakchat.contacts.ContactRepository
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ContactRepository(
        ChatApplication.database.contactDao()
    )
    val contacts: LiveData<List<Contact>> = repository.getContacts()

    fun insert(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }
}