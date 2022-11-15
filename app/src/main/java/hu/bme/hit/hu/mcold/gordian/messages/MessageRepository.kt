package hu.bme.gordian.hu.mcold.gordian.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.szakchat.contacts.Contact
import com.example.szakchat.database.RoomMessage
import com.example.szakchat.extensions.toBase64String
import com.example.szakchat.extensions.toUserID
import hu.bme.gordian.hu.mcold.gordian.ChatApplication
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