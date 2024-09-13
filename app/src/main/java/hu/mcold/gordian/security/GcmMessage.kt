package hu.mcold.gordian.security

import hu.mcold.gordian.common.MyByteArray
import hu.mcold.gordian.login.UserID

data class GcmMessage(
    val version: Int,
    val type: Int,
    val length: Int,
    val seqNum: Int,
    val rnd: MyByteArray,
    val date: Long,
    val src: UserID,
    val dst: UserID,
    val ciphered: MyByteArray,
)