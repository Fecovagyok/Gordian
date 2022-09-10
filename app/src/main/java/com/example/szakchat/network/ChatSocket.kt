package com.example.szakchat.network

import android.content.SharedPreferences
import android.util.Log
import com.example.szakchat.MainActivity
import com.example.szakchat.contacts.Contact
import com.example.szakchat.exceptions.CannotRegister
import com.example.szakchat.messages.Message
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

    private fun auth(writer: BufferedWriter){
        writer.write(PASS)
        writer.newLine()
    }

    fun register() {
        val socket = Socket(ip, PORT)
        val writer = socket.getOutputStream().bufferedWriter()
        auth(writer)
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
        auth(writer)
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

    fun receive(): List<UserWithMessages>{
        if(self == null)
            return listOf()
        val received = mutableListOf<UserWithMessages>()
        val socket = Socket(ip, PORT)
        val writer = socket.getOutputStream().bufferedWriter()
        auth(writer)
        writer.receive()

        val reader = socket.getInputStream().bufferedReader()
        while (true){
            val header = reader.read()
            if(header == END)
                break
            if (header != EXPECT){
                Log.e("FECO", "The first sent integer is not EXPECT")
                break
            }
            // He does not know if the user exists
            val id = reader.readLine()
            val list = mutableListOf<String>()
            while (true) {
                val msgHeader = reader.read()
                if(msgHeader == END)
                    break
                if(msgHeader != EXPECT) {
                    Log.e("FECO", "The sent integer before the message is not EXPECT")
                    break
                }
                val text = reader.readLine()
                list.add(text)
            }
            val messages = UserWithMessages(id, list)
            received.add(messages)
        }
        return received
    }

    fun setSelfId(id: String, preferences: SharedPreferences){
        self = id
        preferences.edit().putString(MainActivity.SELF_KEY, id).apply()
    }
}