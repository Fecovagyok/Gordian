package com.example.szakchat.contacts

data class Contact(
    val id: Long = 0,
    val uniqueId: String,
    val name: String = uniqueId,
)
