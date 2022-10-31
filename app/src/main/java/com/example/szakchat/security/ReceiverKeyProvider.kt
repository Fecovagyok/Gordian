package com.example.szakchat.security

class ReceiverKeyProvider(k: MySecretKey, n: Int) : MyKeyProvider(k, n){
    fun getKeyUntil(num: Int): MySecretKey {
        if(num <= sequenceNumber)
            throw IllegalArgumentException("Given sequence number is too low")
        while (sequenceNumber == num) {
            nextKey()
        }
        return baseKey
    }
}