package com.example.szakchat.network

import android.util.Log
import com.example.szakchat.messages.Message
import java.io.BufferedReader
import java.io.BufferedWriter
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

class ChatSocket(private val logger: StatusLogger, var ip: String, var self: String? = null) {
    companion object {
        const val POLLING_PORT = 9983
        const val SENDING_PORT = 9981
        val factory: SocketFactory = SSLSocketFactory.getDefault()
    }

    fun send(messages: List<Message>){
        if(self == null)
            return
        postSendMessage("Trying to send...")
        val socket = factory.createSocket(ip, SENDING_PORT)
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
        postSendMessage("All sent")
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

    private fun postSendMessage(msg: String){
        logger.postMessage("Sending: $msg")
    }

    private fun postReceiveMessage(msg: String){
        logger.postMessage("Receiving: $msg")
    }

    fun receive(): List<UserWithMessages>?{
        self?: return null
        val self = self!!

        val received = mutableListOf<UserWithMessages>()
        postReceiveMessage("Trying to connect...")
        val socket = factory.createSocket(ip, PORT)
        postReceiveMessage("Connected")
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
        postReceiveMessage("All received")
        return received
    }
}