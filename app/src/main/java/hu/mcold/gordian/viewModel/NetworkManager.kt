package hu.mcold.gordian.viewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import haart.bme.hit.hu.mcold.gordian.network.ConnectionStatus
import haart.bme.gordian.hu.mcold.gordian.network.StatusLogger
import haart.bme.hit.hu.mcold.gordian.ChatApplication
import haart.bme.hit.hu.mcold.gordian.common.TYPE_HELLO
import haart.bme.hit.hu.mcold.gordian.common.TYPE_MESSAGE
import haart.bme.hit.hu.mcold.gordian.common.isRunning
import haart.bme.hit.hu.mcold.gordian.common.toBase64String
import haart.bme.hit.hu.mcold.gordian.exceptions.AlreadyRunning
import haart.bme.hit.hu.mcold.gordian.exceptions.AuthError
import haart.bme.hit.hu.mcold.gordian.exceptions.ProtocolException
import haart.bme.hit.hu.mcold.gordian.login.UserID
import haart.bme.hit.hu.mcold.gordian.messages.Message
import haart.bme.hit.hu.mcold.gordian.network.ChatSocket
import haart.bme.hit.hu.mcold.gordian.network.Credentials
import haart.bme.hit.hu.mcold.gordian.network.TimeOutJob
import hu.mcold.gordian.network.convertToMessages
import hu.mcold.gordian.security.GcmMessage
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

    fun checkPollingSync(){
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

    fun sendHello(message: hu.mcold.gordian.security.GcmMessage, timeOutJob: TimeOutJob) {
        chatSocket.send(listOf(message), timeOutJob)
    }

    private fun startSend(message: Message) = viewModel.viewModelScope.launch(Dispatchers.IO) {
        val gcmMessage = withContext(Dispatchers.Default){
            viewModel.security.myProto.encode(message)
        }
        robust("sending") {
            val id = viewModel.messageRepository.insert(message)
            withTimeOut(this) { timeOutJob ->
                chatSocket.send(listOf(gcmMessage), timeOutJob)
            }
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

    fun logout(prefs: SharedPreferences) {
        username = null
        prefs.edit()
            .remove(SELF_KEY)
            .remove(NAME_KEY)
            .apply()
        ChatApplication.safePrefs.edit().remove(PASS_KEY).apply()
        chatSocket.self = null
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
            if(e.message?.matches(Regex("^failed to connect")) == true)
                postError("$act request timed out")
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
        data.postValue(
            ConnectionStatus(
            normal = true,
            message = msg,
        )
        )
    }

    private var helloChannel: Channel<hu.mcold.gordian.security.GcmMessage>? = null

    fun startHelloChannel(){
        helloChannel = Channel(Channel.CONFLATED)
    }

    fun cancelChannel() {
        helloChannel = null
    }

    suspend fun getHelloMessage(): hu.mcold.gordian.security.GcmMessage? {
        return withTimeoutOrNull(30000) {
            val received = helloChannel!!.receive()
            helloChannel = null
            return@withTimeoutOrNull received
        }
    }

    private suspend fun insertHelloMessage(received: List<hu.mcold.gordian.security.GcmMessage>){
        helloChannel?.let { message ->
            val latest = received.maxBy { it.date }
            message.send(latest)
        }
    }

    private suspend fun insertReceived(received: List<hu.mcold.gordian.security.GcmMessage>){
        val userIds = received.map {
            it.src
        }
        val contacts = viewModel.getContacts(userIds)
        val messages = withContext(Dispatchers.Default) {
            hu.mcold.gordian.network.convertToMessages(
                contacts,
                received,
                viewModel.security.myProto
            )
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
                    val received = withTimeOut(this) { timeOutJob ->
                        chatSocket.receive(timeOutJob)
                    }
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
                withTimeOut(this){timeOutJob ->
                    chatSocket.auth(username, password, logger, timeOutJob)
                }
            } catch (e: ProtocolException){
                Log.e("FECO", "ProtocolException: ${e.message} while logging in")
                logger.postError(e.message?: "Unknown auth error")
            } catch (e: IOException){
                Log.e("FECO", "IOException while logging in: ${e.message}")
                if(e.message?.matches(Regex("${ChatSocket.TIMEOUT}ms\\s*\$", RegexOption.MULTILINE)) == true)
                    logger.postError("Logging in request timed out")
                else
                    logger.postError("Logging request failed")
            }
        }
        return authData
    }
}