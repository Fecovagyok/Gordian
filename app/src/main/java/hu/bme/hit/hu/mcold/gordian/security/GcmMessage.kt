package hu.bme.hit.hu.mcold.gordian.security

import hu.bme.hit.hu.mcold.gordian.common.MyByteArray
import hu.bme.hit.hu.mcold.gordian.identity.UserID

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