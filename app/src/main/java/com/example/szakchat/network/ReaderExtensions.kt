package com.example.szakchat.network

import com.example.szakchat.exceptions.ProtocolException
import com.example.szakchat.extensions.toMyByteArray
import com.example.szakchat.extensions.toUserID
import com.example.szakchat.security.GcmMessage
import java.io.IOException
import java.io.InputStream

fun InputStream.throwRead(): Int {
    val red = read()
    if (red == -1)
        throw IOException("Vege")
    return red
}

fun InputStream.readInt16(): Int {
    val input = throwRead() shl 8
    return input or throwRead()
}

fun InputStream.readInt32(): Int {
    val input = readInt16() shl 16
    return input or readInt16()
}

// Read a string from the input stream with a max size of 255
fun InputStream.readString(): String {
    val size = throwRead()
    val bytes = readAndCreateBytes(size)
    return bytes.toString(Charsets.UTF_8)
}

fun InputStream.throwReadBytes(b: ByteArray) {
    val received = read(b)
    if (received != b.size) {
        throw IOException("Incorrect number of bytes received: size: ${b.size} received: $received")
    }
}

fun InputStream.readGcmMessage(): GcmMessage {
    val version = readInt16()
    val type = throwRead()
    val length = readInt16()
    val seqNum = readInt32()
    val rnd = readAndCreateBytes(7).toMyByteArray()
    val src = readAndCreateBytes(8).toUserID()
    val dst = readAndCreateBytes(8).toUserID()
    val ciphered = readAndCreateBytes(length).toMyByteArray()
    /*if(seqNum < 0)
        throw IllegalStateException("Read sequence number bigger than zero")*/
    return GcmMessage(
        version, type, length, seqNum, rnd,
        src, dst, ciphered,
    )
}

fun InputStream.readAllMessages(): List<GcmMessage> {
    val count = readInt16()
    if(count < 0 || count > Short.MAX_VALUE)
        throw ProtocolException("Invalid message count: $count")
    val list = ArrayList<GcmMessage>(count)
    for(i in 0 until count){
        list.add(readGcmMessage())
    }
    return list
}

fun InputStream.readAndCreateBytes(num: Int): ByteArray {
    val bytes = ByteArray(num)
    throwReadBytes(bytes)
    return bytes
}