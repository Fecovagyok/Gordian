package com.example.szakchat.contacts

import com.example.szakchat.identity.UserID
import com.example.szakchat.security.KeyProviders

data class Contact(
    val id: Long = 0,
    val owner: UserID,
    val uniqueId: UserID? = null,
    val name: String = "Unknown",
    val keys: KeyProviders? = null,
)
