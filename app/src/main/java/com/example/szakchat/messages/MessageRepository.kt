package com.example.szakchat.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.szakchat.ChatApplication
import com.example.szakchat.contacts.Contact
import com.example.szakchat.database.MessageDao
import com.example.szakchat.database.RoomMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageRepository() {
    private val dao = ChatApplication.database.messageDao()
    fun getMessages(contact: Contact): LiveData<List<Message>> = dao.getMessages(contact.id)
        .map { roomMessages ->
            roomMessages.map { roomMessage ->
                roomMessage.toDomainModel(contact)
            }
        }

    suspend fun insert(message: Message) = withContext(Dispatchers.IO) {
        dao.insert(message.toRoomModel())
    }

    suspend fun insert(messages: List<Message>) = withContext(Dispatchers.IO){
        val roomMessages = messages.map {
            it.toRoomModel()
        }
        dao.insertAll(roomMessages)
    }

    suspend fun setSent(message: Message) = withContext(Dispatchers.IO){
        dao.setSent(message.id)
    }

    private fun RoomMessage.toDomainModel(contact: Contact) = Message(
        id = id,
        text = text,
        contact = contact,
        incoming = incoming,
        sent = sent,
    )
    private fun Message.toRoomModel() = RoomMessage(
        id = id,
        contactId = contact.id,
        text = text,
        incoming = incoming,
        sent = sent,
    )
}