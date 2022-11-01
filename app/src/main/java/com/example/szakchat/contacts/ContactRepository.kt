package com.example.szakchat.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.szakchat.database.ContactDao
import com.example.szakchat.database.RoomContact
import com.example.szakchat.extensions.toBase64String
import com.example.szakchat.extensions.toUserID
import com.example.szakchat.identity.UserID
import com.example.szakchat.security.MySecretKey
import com.example.szakchat.security.ReceiverKeyProvider
import com.example.szakchat.security.SenderKeyProvider

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
        sendKey = SenderKeyProvider(
            baseKey = MySecretKey(sendKey),
            seqNum = sendNumber,
        ),
        receiveKey = ReceiverKeyProvider(
            baseKey = MySecretKey(receiveKey),
            seqNum = receiveNumber,
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