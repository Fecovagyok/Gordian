package hu.mcold.gordian.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import haart.bme.hit.hu.mcold.gordian.ChatApplication
import haart.bme.hit.hu.mcold.gordian.common.toBase64String
import haart.bme.hit.hu.mcold.gordian.common.toUserID
import haart.bme.hit.hu.mcold.gordian.contacts.Contact
import haart.bme.hit.hu.mcold.gordian.database.RoomMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageRepository {
    private val dao = ChatApplication.database.messageDao()
    fun getMessages(contact: Contact): LiveData<List<Message>> = dao.getMessages(contact.id)
        .map { roomMessages ->
            roomMessages.map { roomMessage ->
                roomMessage.toDomainModel(contact)
            }
        }

    fun insert(message: Message): Long {
        return dao.insert(message.toRoomModel())
    }

    fun remove(msg: Message){
        dao.delete(msg.toRoomModel())
    }

    fun insert(messages: List<Message>) {
        val roomMessages = messages.map {
            it.toRoomModel()
        }
        dao.insertAll(roomMessages)
    }

    suspend fun setSent(id: Long) = withContext(Dispatchers.IO){
        dao.setSent(id)
    }

    private fun RoomMessage.toDomainModel(contact: Contact) = Message(
        id = id,
        text = text,
        contact = contact,
        incoming = incoming,
        owner = owner.toUserID(),
        date = date,
        sent = sent,
    )
    private fun Message.toRoomModel() = RoomMessage(
        id = id,
        contactId = contact.id,
        text = text,
        incoming = incoming,
        owner = owner.values.toBase64String(),
        date = date,
        sent = sent,
    )
}