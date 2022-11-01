package com.example.szakchat.contacts

import com.example.szakchat.identity.UserID
import com.example.szakchat.security.ReceiverKeyProvider
import com.example.szakchat.security.SenderKeyProvider

data class Contact(
    val id: Long = 0,
    val owner: UserID,
    val uniqueId: UserID,
    val name: String = "Unknown",
    val sendKey: SenderKeyProvider,
    val receiveKey: ReceiverKeyProvider,
)
