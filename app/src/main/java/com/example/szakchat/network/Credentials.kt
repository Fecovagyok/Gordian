package com.example.szakchat.network

import com.example.szakchat.identity.MyByteArray

data class Credentials(
    val id: MyByteArray,
    val pass: String,
)
