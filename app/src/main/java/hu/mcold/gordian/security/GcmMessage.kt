package hu.mcold.gordian.security

import haart.bme.hit.hu.mcold.gordian.common.MyByteArray
import haart.bme.hit.hu.mcold.gordian.login.UserID

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