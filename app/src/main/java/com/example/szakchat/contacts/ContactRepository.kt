package com.example.szakchat.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.szakchat.database.ContactDao
import com.example.szakchat.database.RoomContact
import com.example.szakchat.extensions.toBase64String
import com.example.szakchat.extensions.toUserID
import com.example.szakchat.identity.UserID
import com.example.szakchat.security.MyKeyProvider
import com.example.szakchat.security.MySecretKey

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

    fun insertId(id: String): Long {
        return dao.insertContact(RoomContact(
            uniqueId = id,
            name = id,
        ))
    }

    fun getContacts(list: List<UserID>): List<Contact> {
        return dao.getContacts(
            list.map {
                it.values.toBase64String()
            }
        ).map {
            it.toDomainModel()
        }
    }

    private fun RoomContact.toDomainModel() = Contact(
        id = id,
        owner = owner.toUserID(),
        uniqueId = uniqueId.toUserID(),
        name = name,
        sendKey = MyKeyProvider(
            baseKey = MySecretKey(sendKey),
            _seqNum = sendNumber,
        ),
        receiveKey = MyKeyProvider(
            baseKey = MySecretKey(receiveKey),
            _seqNum = receiveNumber,
        ),
    )
    private fun Contact.toRoomModel() = RoomContact(
        id = id,
        name = name,
        owner = owner.values.toBase64String(),
        sendKey = sendKey.lastKey.values,
        sendNumber = sendKey.sequenceNumber,
        receiveKey = receiveKey.lastKey.values,
        receiveNumber = receiveKey.sequenceNumber,
        uniqueId = uniqueId.values.toBase64String(),
    )
}