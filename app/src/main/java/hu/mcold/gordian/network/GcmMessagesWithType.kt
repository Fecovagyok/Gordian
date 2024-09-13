package hu.mcold.gordian.network

import hu.mcold.gordian.security.GcmMessage

class GcmMessagesWithType(count: Int) {
    private val list: Array<MutableList<hu.mcold.gordian.security.GcmMessage>> = Array(2) {
        if(it == 0) ArrayList(count)
        else mutableListOf()
    }
    operator fun get(type: Int) = list[type] as List<hu.mcold.gordian.security.GcmMessage>
    fun add(message: hu.mcold.gordian.security.GcmMessage){
        list[message.type].add(message)
    }
}
