package hu.bme.gordian.hu.mcold.gordian.messages

import com.example.szakchat.contacts.Contact
import com.example.szakchat.identity.UserID

data class Message(
    val id: Long = 0,
    val text: String,
    val contact: Contact,
    val incoming: Boolean,
    val date: Long,
    val owner: UserID,
    var sent: Boolean = true,
)

