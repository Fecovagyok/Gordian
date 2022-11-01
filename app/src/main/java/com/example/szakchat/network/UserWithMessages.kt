package com.example.szakchat.network

import com.example.szakchat.identity.UserID

data class UserWithMessages(
    val userId: UserID,
    val messages: List<String>,
    )
