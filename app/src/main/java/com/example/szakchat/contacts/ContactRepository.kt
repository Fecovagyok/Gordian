package com.example.szakchat.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.szakchat.database.ContactDao
import com.example.szakchat.database.RoomContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepository(private val dao: ContactDao) {
    fun getContacts(): LiveData<List<Contact>> = dao.getContacts()
        .map { roomContacts ->
            roomContacts.map { roomContact ->
                roomContact.toDomainModel()
            }
        }

    suspend fun insert(contact: Contact) = withContext(Dispatchers.IO) {
        dao.insertContact(contact.toRoomModel())
    }

    private fun RoomContact.toDomainModel() = Contact(id, name)
    private fun Contact.toRoomModel() = RoomContact(
        id = id,
        name = name,
        secret = "titok",
    )
}