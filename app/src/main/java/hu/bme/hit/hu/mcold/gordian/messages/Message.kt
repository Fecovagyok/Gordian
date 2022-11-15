package hu.bme.hit.hu.mcold.gordian.messages

import hu.bme.hit.hu.mcold.gordian.contacts.Contact
import hu.bme.hit.hu.mcold.gordian.login.UserID

data class Message(
    val id: Long = 0,
    val text: String,
    val contact: Contact,
    val incoming: Boolean,
    val date: Long,
    val owner: UserID,
    var sent: Boolean = true,
)

