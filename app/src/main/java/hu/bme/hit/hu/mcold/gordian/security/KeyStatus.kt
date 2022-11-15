package hu.bme.hit.hu.mcold.gordian.security

import hu.bme.hit.hu.mcold.gordian.common.MyByteArray


data class KeyStatus(
    val key: MyByteArray,
    val seqNum: Int,
)