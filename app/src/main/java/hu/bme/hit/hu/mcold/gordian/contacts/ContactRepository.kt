package hu.bme.hit.hu.mcold.gordian.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import hu.bme.hit.hu.mcold.gordian.common.toBase64String
import hu.bme.hit.hu.mcold.gordian.common.toUserID
import hu.bme.hit.hu.mcold.gordian.database.ContactDao
import hu.bme.hit.hu.mcold.gordian.database.RoomContact
import hu.bme.hit.hu.mcold.gordian.identity.UserID
import hu.bme.hit.hu.mcold.gordian.security.KeyProviders
import hu.bme.hit.hu.mcold.gordian.security.MySecretKey
import hu.bme.hit.hu.mcold.gordian.security.ReceiverKeyProvider
import hu.bme.hit.hu.mcold.gordian.security.SenderKeyProvider

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