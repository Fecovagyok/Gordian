package hu.mcold.gordian.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.mcold.gordian.R
import hu.mcold.gordian.ChatApplication
import hu.mcold.gordian.common.*
import hu.mcold.gordian.contacts.Contact
import hu.mcold.gordian.contacts.ContactRepository
import hu.mcold.gordian.exceptions.ProtocolException
import hu.mcold.gordian.login.UserID
import hu.mcold.gordian.messages.Message
import hu.mcold.gordian.messages.MessageRepository
import hu.mcold.gordian.security.GcmMessage
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

    fun startHello(contact: Contact): Boolean {
        if(helloJob.isRunning())
            return false
        pairData.value = null
        helloJob = viewModelScope.launch(Dispatchers.Default) {
            val helloMessage = security.myProto.craftHelloMessage(
                keyProvider = contact.keys!!.sender,
                owner = contact.owner,
                id = contact.uniqueId!!
            )
            Log.d("FECO", "Crafted hello message:\n$helloMessage")
            val ackMessage = withContext(Dispatchers.IO){
                networking.startHelloChannel()
                pairData.postValue(StatusMessage(state = MSG, msg = R.string.sending_hello_message))
                withTimeOut(this@launch){ timeOutJob ->
                    networking.sendHello(helloMessage, timeOutJob)
                }
                networking.checkPollingSync()
                pairData.postValue(StatusMessage(state = MSG, msg = R.string.waiting_hello_reply))
                networking.getHelloMessage()
            }
            ackMessage?: run {
                gettingHelloMessageTimedOut()
                return@launch
            }
            withGoodHelloMessage(contact, ackMessage){
                repository.updateContact(contact)
                pairData.postValue(
                    StatusMessage(
                        state = END,
                    )
                )
            }
        }
        return true
    }

    private fun Contact.toContactWithUserID(userID: UserID) = Contact(
        id = id,
        owner = owner,
        uniqueId = userID,
        name = name,
        keys = keys,
    )

    private fun gettingHelloMessageTimedOut(){
        pairData.postValue(
            StatusMessage(
                state = ERROR,
                R.string.pairing_timed_out
            )
        )
    }

    // to be called on the default thread
    private suspend inline fun withGoodHelloMessage(
        contact: Contact,
        helloMessage: hu.mcold.gordian.security.GcmMessage,
        block: suspend (Contact) -> Unit,
    ) {
        try {
            security.myProto.decodeHelloMessage(helloMessage, contact.keys!!.receiver)
            val newContact = contact.toContactWithUserID(helloMessage.src)
            currentContact = newContact
            block(newContact)
        } catch (e: javax.crypto.AEADBadTagException) {
            Log.e("FECO", "Bad message:\n$helloMessage")
            pairData.postValue(
                StatusMessage(
                    state = ERROR,
                    msg = R.string.hello_authenticate_failed
                )
            )
        } catch (e: ProtocolException) {
            pairData.postValue(
                StatusMessage(
                    state = ERROR,
                    msg = R.string.hello_message_had_payload
                )
            )
        }
    }

    fun listenHello(contact: Contact): Boolean {
        if(helloJob.isRunning())
            return false
        pairData.value = null
        helloJob = viewModelScope.launch(Dispatchers.IO) {
            pairData.postValue(StatusMessage(state = MSG, R.string.waiting_hello_message))
            val helloMessage = networking.getHelloMessage()
            helloMessage?: run {
                gettingHelloMessageTimedOut()
                return@launch
            }
            withContext(Dispatchers.Default) {
                withGoodHelloMessage(contact, helloMessage) { newContact ->
                    val myHello = security.myProto.craftHelloMessage(
                        keyProvider = contact.keys!!.sender,
                        owner = newContact.owner,
                        id = newContact.uniqueId!!
                    )
                    withContext(Dispatchers.IO) {
                        repository.updateContact(newContact)
                        withTimeOut(this@launch){timeOutJob ->
                            networking.sendHello(myHello, timeOutJob) // Sending the ack
                        }
                    }
                    pairData.postValue(StatusMessage(state = END,))
                }
            }
        }
        return true
    }

}