package com.example.szakchat.viewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.szakchat.ChatApplication
import com.example.szakchat.common.TYPE_HELLO
import com.example.szakchat.common.TYPE_MESSAGE
import com.example.szakchat.exceptions.AlreadyRunning
import com.example.szakchat.exceptions.AuthError
import com.example.szakchat.exceptions.ProtocolException
import com.example.szakchat.extensions.isRunning
import com.example.szakchat.extensions.toBase64String
import com.example.szakchat.identity.UserID
import com.example.szakchat.messages.Message
import com.example.szakchat.network.*
import com.example.szakchat.security.GcmMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLProtocolException


class NetworkManager(private val viewModel: ChatViewModel, ip: String)
    : StatusLogger {

    companion object {
        const val SELF_KEY = "SELF_KEY"
        const val DEFAULT_IP = "89.133.85.78"
        const val IP_KEY = "SERVER_ADDRESS"
        const val POLL_INTERVAL = 3000L //ms
        const val CHECK_INTERVAL = 80L /* sec */ * 1000L
        const val NAME_KEY = "NAME_KEY"
        const val PASS_KEY = "PASS_KEY"
    }

    private val chatSocket = ChatSocket(this, ip)

    fun syncPostMessage(msg: String ) {
        _networkStatus.value = ConnectionStatus(
            normal = true,
            message = msg,
        )
    }

    fun syncPostError(msg: String) {
        _networkStatus.value = ConnectionStatus(
            normal = true,
            message = msg,
        )
    }

    var ip
    get() = chatSocket.ip
    set(value) { chatSocket.ip = value }

    var username: String? = null

    val self
    get() = chatSocket.self?.id

    private var pollJob: Job? = null
    private var sendJob: Job? = null
    private val checkPollChannel = Channel<Unit>(Channel.CONFLATED)

    private val _networkStatus = MutableLiveData<ConnectionStatus>()
    val networkStatus: LiveData<ConnectionStatus>
    get() = _networkStatus

    private fun checkPollingSync(){
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            checkPollChannel.send(Unit)
        }
    }

    fun send(message: Message): Boolean {
        checkPollingSync()
        if(sendJob == null){
            sendJob = startSend(message)
            return true
        }
        if(sendJob!!.isCompleted){
            sendJob = startSend(message)
            return true
        }
        viewModel.insertMessage(message)
        return false
    }

    fun sendHello(message: GcmMessage) {
        chatSocket.send(listOf(message))
    }

    private fun startSend(message: Message) = viewModel.viewModelScope.launch(Dispatchers.IO) {
        val gcmMessage = withContext(Dispatchers.Default){
            viewModel.security.myProto.encode(message)
        }
        robust("sending") {
            val id = viewModel.messageRepository.insert(message)
            chatSocket.send(listOf(gcmMessage))
            message.sent = true
            viewModel.messageRepository.setSent(id)
        }
    }

    fun initSelfCredentials(id: UserID, pass: String){
        chatSocket.self = Credentials(id, pass)
    }

    fun setSelfCredentialsPermanently(name: String, pass: String, prefs: SharedPreferences){
        username = name
        prefs.edit()
            .putString(SELF_KEY, self!!.values.toBase64String())
            .putString(NAME_KEY, name)
            .apply()
        ChatApplication.safePrefs.edit().putString(PASS_KEY, pass).apply()
    }

    override fun postError(msg: String){
        postError(msg, _networkStatus)
    }

    fun postError(msg: String, data: MutableLiveData<ConnectionStatus>) {
        data.postValue(
            ConnectionStatus(
                normal = false,
                message = msg,
            )
        )
    }

    private inline fun robust(act: String, block: () -> Unit){
        try {
            block()
        } catch (e: ConnectException) {
            Log.e("FECO", "ConnectionException: ${e.message}")
            postError("Could not connect while $act")
        } catch (e: SocketTimeoutException) {
            Log.e("FECO", "SocketTimeoutException: ${e.message}")
            postError("Could not connect: timed out while $act")
        } catch (e: SSLProtocolException){
            Log.e("FECO", e.message?: "SSLProtocol unknown")
            if(e.message?.substring(0, 10) == "Read error")
                postError("Socket closed from remote end")
            else
                postError(e.message?: "Unknown TLS socket error")
        } catch (e: IOException){
            Log.e("FECO", "ErrorMessage: ${e.message} Cause ${e.cause} Type: ${e.javaClass}")
            e.printStackTrace()
            postError("${e.message?: "No message"} while $act")
        } catch (e: IllegalStateException){
            Log.e("FECO", "ErrorMessage: ${e.message} Cause ${e.cause} Type: ${e.javaClass}")
            postError("${e.message?: "No message"} while $act")
        } catch (e: AuthError) {
            Log.e("FECO", "ErrorMessage: ${e.message} Cause ${e.cause} Type: ${e.javaClass}")
            e.printStackTrace()
            postError("${e.message?: "No message"} while $act")
        }
    }

    override fun postMessage(msg: String){
        postMessage(msg, _networkStatus)
    }

    fun postMessage(msg: String, data: MutableLiveData<ConnectionStatus>) {
        data.postValue(ConnectionStatus(
            normal = true,
            message = msg,
        ))
    }

    private var helloChannel: Channel<GcmMessage>? = null

    suspend fun getHelloMessage(): GcmMessage {
        val channel = Channel<GcmMessage>(Channel.CONFLATED)
        helloChannel = channel
        val received = channel.receive()
        helloChannel = null
        return received
    }

    private suspend fun insertHelloMessage(received: List<GcmMessage>){
        helloChannel?.let {
            val latest = received.maxBy { it.date }
            it.send(latest)
        }
    }

    private suspend fun insertReceived(received: List<GcmMessage>){
        val userIds = received.map {
            it.src
        }
        val contacts = viewModel.getContacts(userIds)
        val messages = withContext(Dispatchers.Default) {
            convertToMessages(contacts, received, viewModel.security.myProto)
        }
        viewModel.messageRepository.insert(messages)
    }

    fun startPollStartJob(){
        startPolling()
        startPollJobTicker()
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            while(true){
                checkPollChannel.receive()
                if(pollJob!!.isCompleted)
                    startPolling()
            }
        }
    }

    private fun startPollJobTicker() = viewModel.viewModelScope.launch(Dispatchers.IO) {
        while(true){
            delay(CHECK_INTERVAL)
            checkPollChannel.send(Unit)
        }
    }

    private fun startPolling(){
        pollJob =  viewModel.viewModelScope.launch(Dispatchers.IO) {
            robust("polling") {
                while (true) {
                    delay(POLL_INTERVAL)
                    val received = chatSocket.receive()
                    if(received[TYPE_HELLO].isNotEmpty())
                        insertHelloMessage(received[TYPE_HELLO])
                    if(received[TYPE_MESSAGE].isNotEmpty())
                        insertReceived(received[TYPE_MESSAGE])
                }
            }
        }
    }

    private val _authData by lazy(LazyThreadSafetyMode.NONE){
        MutableLiveData<ConnectionStatus>()
    }
    val authData get() = _authData as LiveData<ConnectionStatus>

    private var loginJob: Job? = null
    fun loginRequest(username: String, password: String): LiveData<ConnectionStatus> {
        if(loginJob.isRunning())
            throw AlreadyRunning("Login request already in progress")
        loginJob = viewModel.viewModelScope.launch(Dispatchers.IO) {
            val logger = object : StatusLogger {
                override fun postError(msg: String) {
                    postError(msg, _authData)
                }

                override fun postMessage(msg: String) {
                    postMessage(msg, _authData)
                }
            }
            try {
                chatSocket.auth(username, password, logger)
            } catch (e: ProtocolException){
                Log.e("FECO", "ProtocolException: ${e.message} while logging in")
                logger.postError(e.message?: "Unknown auth error")
            } catch (e: IOException){
                Log.e("FECO", "IOException while logging in: ${e.message}")
                logger.postError(e.message?: "Unknown network error")
            }
        }
        return authData
    }
}