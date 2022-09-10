package com.example.szakchat.messages

import com.example.szakchat.contacts.Contact

data class Message(
    val id: Long = 0,
    val text: String,
    val contact: Contact,
    val incoming: Boolean,
    val sent: Boolean? = null,
)

