package com.example.szakchat.security

import com.example.szakchat.common.MyByteArray
import com.example.szakchat.identity.UserID

data class GcmMessage(
    val version: Int,
    val type: Byte,
    val length: Int,
    val seqNum: Int,
    val rnd: MyByteArray,
    val src: UserID,
    val dst: UserID,
    val ciphered: MyByteArray,
)