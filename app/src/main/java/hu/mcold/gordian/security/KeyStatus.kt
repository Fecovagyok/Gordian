package hu.mcold.gordian.security

import haart.bme.hit.hu.mcold.gordian.common.MyByteArray


data class KeyStatus(
    val key: MyByteArray,
    val seqNum: Int,
)