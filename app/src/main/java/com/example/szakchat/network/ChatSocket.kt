package com.example.szakchat.network

import android.util.Log
import com.example.szakchat.exceptions.AuthError
import com.example.szakchat.exceptions.ProtocolException
import com.example.szakchat.extensions.awaitClose
import com.example.szakchat.extensions.toUserID
import com.example.szakchat.security.GcmMessage
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

class ChatSocket(private val logger: StatusLogger, var ip: String, var self: Credentials? = null) {
    companion object {
        const val POLLING_PORT = 9983
        const val SENDING_PORT = 9981
        private const val AUTH_WITH_ID = 3
        private const val AUTH_WITH_NAME = 2
        private const val AUTH_ONLY = 10
        private const val AUTH_OK = 20
        private const val AUTH_NOK = 22
        val factory: SocketFactory = SSLSocketFactory.getDefault()
    }

    fun send(messages: List<GcmMessage>){
        postReceiveMessage("Trying to connect...")
        val socket = factory.createSocket(ip, SENDING_PORT)
        postReceiveMessage("Connected")
        val inS = socket.getInputStream()
        val out = socket.getOutputStream()
        withAuth(out, inS) {
            out.writeAll(messages)
            socket.awaitClose()
        }
        postSendMessage("All sent")
    }

    private fun postSendMessage(msg: String){
        logger.postMessage("Sending: $msg")
    }

    private fun postReceiveMessage(msg: String){
        logger.postMessage("Receiving: $msg")
    }

    fun receive(): List<GcmMessage> {
        postReceiveMessage("Trying to connect...")
        val socket = factory.createSocket(ip, POLLING_PORT)
        postReceiveMessage("Connected")
        val inS = socket.getInputStream()
        val out = socket.getOutputStream()
        val received = withAuth(out = out, inS = inS) {
            inS.readAllMessages()
        }
        socket.close()
        postReceiveMessage("All received")
        return received
    }

    private inline fun <T> withAuth(out: OutputStream, inS: InputStream, block: () -> T): T{
        return if(auth(out, inS)){
            block()
        } else {
            val msg = inS.readString()
            throw AuthError(msg)
        }
    }

    private fun auth(out: OutputStream, iStream: InputStream): Boolean {
        Log.d("FECO", "Credentials in socket: $self")
        val meSelf = self?: throw IllegalStateException("No logged on user")
        out.write(AUTH_WITH_ID)
        out.write(meSelf.id.values)
        out.writeString(meSelf.pass)
        out.write(AUTH_ONLY+4)
        return when(iStream.throwRead()) {
            AUTH_OK -> true
            AUTH_NOK -> false
            else -> throw IOException("Bad auth message")
        }
    }

    fun auth(username: String, password: String, out: StatusLogger){
        val socket = factory.createSocket(ip, POLLING_PORT)
        val outStream = socket.getOutputStream()
        val inStream = socket.getInputStream()
        outStream.write(AUTH_WITH_NAME)
        outStream.writeString(username)
        outStream.writeString(password)
        when(inStream.throwRead()) {
            AUTH_OK -> {
                outStream.write(AUTH_ONLY)
                val myId = inStream.readAndCreateBytes(8)
                self = Credentials(id = myId.toUserID(), password)
                out.postMessage("Success")
            }

            AUTH_NOK -> {
                val msg = inStream.readString()
                out.postError("Auth error: $msg")
            }

            else -> throw ProtocolException("Unknown auth message")
        }
    }
}