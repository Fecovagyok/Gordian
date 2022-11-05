package com.example.szakchat.security

import android.util.Log

class ReceiverKeyProvider(baseKey: MySecretKey, seqNum: Int) : MyKeyProvider(baseKey, seqNum){
    fun getKeyUntil(num: Int): MySecretKey {
        if(num <= sequenceNumber)
            throw IllegalArgumentException("Given sequence number is too low")
        while (sequenceNumber == num) {
            Log.d("FECO", "Generating a key, seqnum: $_seqNum")
            nextKey()
        }
        return baseKey
    }
}