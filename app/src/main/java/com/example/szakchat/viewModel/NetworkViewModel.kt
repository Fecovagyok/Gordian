package com.example.szakchat.viewModel

import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.example.szakchat.contacts.Contact
import com.example.szakchat.messages.Message
import com.example.szakchat.network.ChatSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NetworkViewModel(private val viewModel: ChatViewModel) {

    companion object {
        const val SELF_KEY = "SELF_KEY"
        const val DEFAULT_IP = "89.133.85.78"
    }

    private val chatSocket = ChatSocket(DEFAULT_IP)
    var ip
    get() = chatSocket.ip
    set(value) { chatSocket.ip = value }

    var self
    get() = chatSocket.self
    set(value) { chatSocket.self = value}

    var pollJob: Job? = null
    private set

    fun send(message: Message) = viewModel.viewModelScope.launch(Dispatchers.IO) {
        viewModel.messageRepository.insert(message)
        chatSocket.send(listOf(message))
        viewModel.messageRepository.setSent(message)
    }

    fun setSelfId(value: String, prefs: SharedPreferences){
        self = value
        prefs.edit().putString(SELF_KEY, value).apply()
    }

    fun startPolling(){
        pollJob =  viewModel.viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(4500)
                val received = chatSocket.receive()?: continue
                val userIds = received.map {
                    it.userId
                }
                val contacts = viewModel.getContacts(userIds)
                // To Spawn the contacts into existence
                val map = HashMap<String, Contact>()
                contacts.forEach { map[it.uniqueId] = it }
                received.forEach {
                    val contact = map[it.userId]?: run {
                        val c = Contact(uniqueId = it.userId)
                        viewModel.repository.insert(c)
                        c
                    }
                    val toInsert = it.messages.map { text ->
                        Message(
                            contact = contact,
                            text = text,
                            incoming = true
                        )
                    }
                    viewModel.messageRepository.insert(toInsert)
                }
            }
        }
    }


}