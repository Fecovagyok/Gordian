package com.example.szakchat.network

import com.example.szakchat.contacts.Contact
import com.example.szakchat.messages.Message

data class UserWithMessages(
    val userId: String,
    val messages: List<String>,
    )
