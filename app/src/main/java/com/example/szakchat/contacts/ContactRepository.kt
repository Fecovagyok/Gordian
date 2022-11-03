package com.example.szakchat.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.szakchat.database.ContactDao
import com.example.szakchat.database.RoomContact
import com.example.szakchat.extensions.toBase64String
import com.example.szakchat.extensions.toUserID
import com.example.szakchat.identity.UserID
import com.example.szakchat.security.KeyProviders
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

    fun updateContact(contact: Contact) {
        dao.updateContact(contact.toRoomModel())
    }

    private fun RoomContact.toDomainModel() = Contact(
        id = id,
        owner = owner.toUserID(),
        uniqueId = uniqueId?.toUserID(),
        name = name,
        keys = if(sendNumber < 0 || receiveNumber < 0) null else
            KeyProviders(
                sender = SenderKeyProvider(
                baseKey = MySecretKey(sendKey!!),
                seqNum = sendNumber,
            ),
                receiver = ReceiverKeyProvider(
                baseKey = MySecretKey(receiveKey!!),
                seqNum = receiveNumber,
            ),
        )
    )
    private fun Contact.toRoomModel() = RoomContact(
        id = id,
        name = name,
        owner = owner.values.toBase64String(),
        sendKey = keys?.sender?.lastKey?.values,
        sendNumber = keys?.sender?.sequenceNumber?: -1,
        receiveKey = keys?.receiver?.lastKey?.values,
        receiveNumber = keys?.receiver?.sequenceNumber?: -1,
        uniqueId = uniqueId?.values?.toBase64String(),
    )
}