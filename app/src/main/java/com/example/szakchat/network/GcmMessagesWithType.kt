package com.example.szakchat.network

import android.util.Log
import com.example.szakchat.security.GcmMessage

class GcmMessagesWithType(count: Int) {
    private val list: Array<MutableList<GcmMessage>> = Array(2) {
        if(it == 0) ArrayList(count)
        else mutableListOf()
    }
    operator fun get(type: Int) = list[type] as List<GcmMessage>
    fun add(message: GcmMessage){
        Log.d("FECO", "Received message: type: ${message.type}")
        list[message.type].add(message)
    }
}
