package com.example.szakchat.network

import java.io.BufferedWriter

fun BufferedWriter.register() {
    write(ChatSocketCommands.REGISTER)
    newLine()
}

fun BufferedWriter.send() {
    write(ChatSocketCommands.SEND)
    newLine()
}

fun BufferedWriter.receive() {
    write(ChatSocketCommands.REGISTER)
    newLine()
}

fun BufferedWriter.writeLine(string: String) {
    write(string)
    newLine()
}