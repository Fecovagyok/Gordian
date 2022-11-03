package com.example.szakchat.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.szakchat.ChatApplication
import com.example.szakchat.R
import com.example.szakchat.common.END
import com.example.szakchat.common.ERROR
import com.example.szakchat.common.MSG
import com.example.szakchat.common.StatusMessage
import com.example.szakchat.contacts.Contact
import com.example.szakchat.contacts.ContactRepository
import com.example.szakchat.exceptions.AlreadyRunning
import com.example.szakchat.exceptions.ProtocolException
import com.example.szakchat.extensions.isRunning
import com.example.szakchat.identity.UserID
import com.example.szakchat.messages.Message
import com.example.szakchat.messages.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel() : ViewModel() {

    private var _networking: NetworkManager? = null
    val networking get() = _networking!!
    val security = MySecurityManager(this)
    var currentMessages: LiveData<List<Message>>? = null
        private set
    val messageRepository = MessageRepository()
    var currentContact: Contact? = null
    set(value) {
        field = value
        value?.let {
            currentMessages = messageRepository.getMessages(it)
        }
    }
    val repository = ContactRepository(
        ChatApplication.database.contactDao()
    )
    val contacts: LiveData<List<Contact>> = repository.getContacts()

    fun initNetwork(ip: String){
        _networking?: run {
            _networking = NetworkManager(this, ip)
            networking.startPollStartJob()
        }
    }

    fun getContacts(list: List<UserID>) = repository.getContacts(list)

    fun insertContact(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(contact)
    }
    fun insertMessage(message: Message) = viewModelScope.launch(Dispatchers.IO) {
        messageRepository.insert(message)
    }

    fun removeMessage(msg: Message) = viewModelScope.launch(Dispatchers.IO) {
        messageRepository.remove(msg)
    }

    private var helloJob: Job? = null
    val pairData = MutableLiveData<StatusMessage?>()

    fun startHello(contact: Contact) {
        if(helloJob.isRunning())
            throw AlreadyRunning("PairingJob is already running")
        pairData.value = null
        helloJob = viewModelScope.launch(Dispatchers.Default) {
            val helloMessage = security.myProto.craftHelloMessage(
                keyProvider = contact.keys!!.sender,
                owner = contact.owner,
                id = contact.uniqueId!!
            )
            withContext(Dispatchers.IO){
                pairData.postValue(StatusMessage(state = MSG, msg = R.string.sending_hello_message))
                networking.sendHello(helloMessage)
                pairData.postValue(StatusMessage(state = MSG, msg = R.string.waiting_hello_reply))

            }
        }
    }

    private fun Contact.toContactWithUserID(userID: UserID) = Contact(
        id = id,
        owner = owner,
        uniqueId = userID,
        name = name,
        keys = keys,
    )

    fun listenHello(contact: Contact) {
        if(helloJob.isRunning())
            throw AlreadyRunning("PairingJob is already running")
        pairData.value = null
        helloJob = viewModelScope.launch(Dispatchers.IO) {
            val helloMessage = networking.getHelloMessage()
            withContext(Dispatchers.Default) {
                try {
                    security.myProto.decodeHelloMessage(helloMessage, contact.keys!!.receiver)
                    val newContact = contact.toContactWithUserID(helloMessage.src)
                    currentContact = newContact
                    withContext(Dispatchers.IO){
                        repository.updateContact(newContact)
                    }
                    pairData.postValue(StatusMessage(state = END,))

                } catch (e: javax.crypto.AEADBadTagException){
                    pairData.postValue(StatusMessage(state = ERROR, msg = R.string.hello_authenticate_failed))
                } catch (e: ProtocolException){
                    pairData.postValue(StatusMessage(state = ERROR, msg = R.string.hello_message_had_payload))
                }
            }
        }
    }
}