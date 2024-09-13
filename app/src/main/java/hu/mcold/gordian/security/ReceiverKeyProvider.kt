package hu.mcold.gordian.security

import android.util.Log

class ReceiverKeyProvider(baseKey: MySecretKey, seqNum: Int) : MyKeyProvider(baseKey, seqNum){
    fun getKeyUntil(num: Int): MySecretKey {
        if(num <= sequenceNumber)
            throw IllegalArgumentException("Given sequence number is too low")
        while (num > sequenceNumber) {
            Log.d("FECO", "Generating a key, seqnum: $_seqNum")
            nextKey()
        }
        return baseKey
    }
}