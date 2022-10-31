package com.example.szakchat.security

import java.security.MessageDigest

abstract class MyKeyProvider(protected var baseKey: MySecretKey, protected var _seqNum: Int = 0) {
    val sequenceNumber get() = _seqNum
    val lastKey get() = baseKey
    private val digester: MessageDigest = MessageDigest.getInstance("SHA-256")
    fun nextKey(): MySecretKey {
        val newKey = digester.digest(baseKey.values)
        baseKey.setCanBeDestroyed(true, this)
        baseKey = MySecretKey(newKey)
        _seqNum++
        return baseKey
    }
}