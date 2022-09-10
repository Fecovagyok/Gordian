package com.example.szakchat.network

import android.content.SharedPreferences
import android.util.Log
import com.example.szakchat.MainActivity
import com.example.szakchat.contacts.Contact
import com.example.szakchat.exceptions.CannotRegister
import com.example.szakchat.messages.Message
import com.example.szakchat.viewModel.ChatViewModel
import com.example.szakchat.viewModel.NetworkViewModel
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

class ChatSocket(var ip: String, var self: String? = null) {
    companion object {
        const val PORT = 9983
        const val PASS = "kalapacsos"
        const val GOOD = 200
        const val TAKEN = 300
        const val END = 17
        const val EXPECT = 8
    }

    fun register() {
        val socket = Socket(ip, PORT)
        val writer = socket.getOutputStream().bufferedWriter()
        writer.auth()
        writer.apply {
            register()
            flush()
        }
        val result = socket.getInputStream().read()
        if(result != GOOD)
            throw CannotRegister("Received: $result")
    }

    fun send(messages: List<Message>){
        if(self == null)
            return
        val socket = Socket(ip, PORT)
        val writer = socket.getOutputStream().bufferedWriter()
        writer.auth()
        writer.apply {
            send()
            writeLine(self!!)
            for (msg in messages) {
                writeLine(msg.contact.uniqueId)
                writeLine(msg.text)
            }
            flush()
        }
        socket.close()
    }

    private fun checkEnd(reader: BufferedReader, writer: BufferedWriter): Boolean {
        val header = reader.read()
        if(header == END) {
            writer.apply { write(END); flush() }
            return true
        }
        if (header != EXPECT){
            Log.e("FECO", "The first sent integer is not EXPECT: $header")
            return true
        }
        return false
    }

    fun receive(): List<UserWithMessages>?{
        self?: return null
        val self = self!!

        val received = mutableListOf<UserWithMessages>()
        val socket = Socket(ip, PORT)
        val writer = socket.getOutputStream().bufferedWriter()
        writer.auth()
        writer.receive(self)

        val reader = socket.getInputStream().bufferedReader()
        while (true){
            if(checkEnd(reader, writer))
                break
            // He does not know if the user exists
            val id = reader.readLine()
            val list = mutableListOf<String>()
            while (true) {
                if(checkEnd(reader, writer))
                    break

                val text = reader.readLine()
                list.add(text)
            }
            val messages = UserWithMessages(id, list)
            received.add(messages)
        }
        return received
    }
}