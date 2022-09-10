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

    fun insert(contact: Contact): Long  {
        return dao.insertContact(contact.toRoomModel())
    }

    fun getContacts(list: List<String>): List<Contact> {
        return dao.getContacts(list).map {
            it.toDomainModel()
        }
    }

    private fun RoomContact.toDomainModel() = Contact(id, name, uniqueId)
    private fun Contact.toRoomModel() = RoomContact(
        id = id,
        name = name,
        uniqueId = uniqueId,
    )
}