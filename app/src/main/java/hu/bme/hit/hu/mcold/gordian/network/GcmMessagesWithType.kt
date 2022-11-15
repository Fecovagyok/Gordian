package hu.bme.hit.hu.mcold.gordian.network

import hu.bme.hit.hu.mcold.gordian.security.GcmMessage

class GcmMessagesWithType(count: Int) {
    private val list: Array<MutableList<GcmMessage>> = Array(2) {
        if(it == 0) ArrayList(count)
        else mutableListOf()
    }
    operator fun get(type: Int) = list[type] as List<GcmMessage>
    fun add(message: GcmMessage){
        list[message.type].add(message)
    }
}
