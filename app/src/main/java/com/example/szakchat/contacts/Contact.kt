package com.example.szakchat.contacts

import com.example.szakchat.common.MyByteArray
import com.example.szakchat.identity.UserID
import com.example.szakchat.security.MyKeyProvider

data class Contact(
    val id: Long = 0,
    val owner: MyByteArray,
    val uniqueId: UserID,
    val name: String = "Unknown",
    val sendKey: MyKeyProvider,
    val receiveKey: MyKeyProvider,
)
