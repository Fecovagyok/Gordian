package com.example.szakchat.network

import java.io.BufferedWriter

fun BufferedWriter.register() {
    write(ChatSocketCommands.REGISTER)
    newLine()
}

fun BufferedWriter.auth() {
    write(ChatSocket.PASS)
    newLine()
    flush()
}

fun BufferedWriter.send() {
    write(ChatSocketCommands.SEND)
    newLine()
}

fun BufferedWriter.receive(selfId: String) {
    write(ChatSocketCommands.RECEIVE)
    newLine()
    write(selfId)
    newLine()
    flush()
}

fun BufferedWriter.writeLine(string: String) {
    write(string)
    newLine()
}