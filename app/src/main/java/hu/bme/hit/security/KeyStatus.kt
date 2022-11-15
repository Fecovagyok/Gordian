package hu.bme.hit.security

import com.example.szakchat.common.MyByteArray

data class KeyStatus(
    val key: MyByteArray,
    val seqNum: Int,
)