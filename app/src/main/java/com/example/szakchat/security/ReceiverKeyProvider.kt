package com.example.szakchat.security

class ReceiverKeyProvider(baseKey: MySecretKey, seqNum: Int) : MyKeyProvider(baseKey, seqNum){
    fun getKeyUntil(num: Int): MySecretKey {
        if(num <= sequenceNumber)
            throw IllegalArgumentException("Given sequence number is too low")
        while (sequenceNumber == num) {
            nextKey()
        }
        return baseKey
    }
}