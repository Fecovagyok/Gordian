package com.example.szakchat.contacts

data class Contact(
    val id: Int,
    var name: String = id.toString(),
)
