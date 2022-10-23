package com.example.szakchat.network

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

fun InputStream.readAndCreateBytes(num: Int): ByteArray {
    val bytes = ByteArray(num)
    throwReadBytes(bytes)
    return bytes
}