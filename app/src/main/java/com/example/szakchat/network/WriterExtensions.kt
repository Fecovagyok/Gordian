package com.example.szakchat.network

import com.example.szakchat.common.MyByteArray
import com.example.szakchat.security.GcmMessage
import java.io.OutputStream

fun OutputStream.writeString(str: String) {
    val array = str.toByteArray(Charsets.UTF_8)
    if (array.size > 126)
        throw IllegalArgumentException("The written string can be max 126 bytes")
    write(array.size)
    write(array)
}

fun OutputStream.writeInt16(value: Int) {
    val left = value ushr 8
    write(left)
    write(value)
}

fun OutputStream.writeInt32(value: Int) {
    writeInt16(value ushr 16)
    writeInt16(value)
}

fun OutputStream.write(b: MyByteArray) = write(b.values)

fun OutputStream.writeGcmMessage(msg: GcmMessage){
    writeInt16(msg.version)
    write(msg.type)
    writeInt16(msg.length)
    writeInt32(msg.seqNum)
    write(msg.rnd)
    write(msg.src)
    write(msg.dst)
    write(msg.ciphered)
}

// With fear, that it will be too less, the message count is sent as 16 bit value
fun OutputStream.writeAll(list: List<GcmMessage>) {
    if (list.size > Short.MAX_VALUE)
        throw IllegalArgumentException("Message count exceeded 16 bits")
    writeInt16(list.size)
    list.forEach {
        writeGcmMessage(it)
    }
}