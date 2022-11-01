package com.example.szakchat.viewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.szakchat.ChatApplication
import com.example.szakchat.exceptions.AlreadyRunning
import com.example.szakchat.exceptions.AuthError
import com.example.szakchat.extensions.isRunning
import com.example.szakchat.extensions.toUserID
import com.example.szakchat.identity.UserID
import com.example.szakchat.messages.Message
import com.example.szakchat.network.*
import com.example.szakchat.security.GcmMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException


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

    fun setSelfCredentialsPermanently(id: String, name: String, pass: String, prefs: SharedPreferences){
        chatSocket.self = Credentials(id.toUserID(), pass)
        username = name
        prefs.edit()
            .putString(SELF_KEY, id)
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
        } catch (e: SocketTimeoutException){
            Log.e("FECO", "SocketTimeoutException: ${e.message}")
            postError("Could not connect: timed out while $act")
        } catch (e: IOException){
            Log.e("FECO", "ErrorMessage: ${e.message} Cause ${e.cause} Type: ${e.javaClass}")
            e.printStackTrace()
            postError("${e.message?: "No message"} while $act")
        } catch (e: IllegalStateException){
            Log.e("FECO", "ErrorMessage: ${e.message} Cause ${e.cause} Type: ${e.javaClass}")
            e.printStackTrace()
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
                    if(received.isEmpty()) continue
                    insertReceived(received)
                }
            }
        }
    }


    private var loginJob: Job? = null
    fun loginRequest(username: String, password: String): LiveData<ConnectionStatus> {
        if(loginJob.isRunning())
            throw AlreadyRunning("Login request already in progress")
        val data = MutableLiveData<ConnectionStatus>()
        loginJob = viewModel.viewModelScope.launch {
            val logger = object : StatusLogger {
                override fun postError(msg: String) {
                    postError(msg, data)
                }

                override fun postMessage(msg: String) {
                    postMessage(msg, data)
                }
            }
            robust("Logging in") {
                chatSocket.auth(username, password, logger)
            }
        }
        return data
    }
}