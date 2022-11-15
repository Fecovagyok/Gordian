package hu.bme.gordian.hu.mcold.gordian.common

data class StatusMessage(
    val state: Int,
    val msg: Int = 0,
)

const val START = 2
const val END = 1
const val MSG = 3
const val ERROR = 4
