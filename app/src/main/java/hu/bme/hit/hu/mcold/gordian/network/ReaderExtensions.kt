package hu.bme.hit.hu.mcold.gordian.network

import hu.bme.hit.hu.mcold.gordian.common.myToLong
import hu.bme.hit.hu.mcold.gordian.common.toMyByteArray
import hu.bme.hit.hu.mcold.gordian.common.toUserID
import hu.bme.hit.hu.mcold.gordian.exceptions.ProtocolException
import hu.bme.hit.hu.mcold.gordian.security.GcmMessage
import java.io.IOException
import java.io.InputStream

fun InputStream.throwRead(): Int {
    val red = read()
    if (red == -1)
        throw IOException("Remote end closed the socket")
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

fun InputStream.readLong(): Long {
    val input = readInt32().toLong() shl 32
    return input or readInt32().myToLong()
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
    val rnd = readAndCreateBytes(8).toMyByteArray()
    val date = readLong()
    val src = readAndCreateBytes(8).toUserID()
    val dst = readAndCreateBytes(8).toUserID()
    val ciphered = readAndCreateBytes(length).toMyByteArray()
    /*if(seqNum < 0)
        throw IllegalStateException("Read sequence number bigger than zero")*/
    return GcmMessage(
        version, type, length, seqNum, rnd, date,
        src, dst, ciphered,
    )
}

fun InputStream.readAllMessages(): GcmMessagesWithType {
    val count = readInt16()
    if(count < 0 || count > Short.MAX_VALUE)
        throw ProtocolException("Invalid message count: $count")
    val list = GcmMessagesWithType(count)
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