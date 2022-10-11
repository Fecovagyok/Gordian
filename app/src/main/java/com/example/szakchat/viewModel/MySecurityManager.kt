package com.example.szakchat.viewModel

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.szakchat.R
import com.example.szakchat.extensions.isRunning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.security.SecureRandom

class MySecurityManager(private val viewModel: ChatViewModel) {

    companion object {
        const val START = 2
        const val END = 1
        const val MSG = 3
    }

    private val random: SecureRandom by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SecureRandom.getInstanceStrong()
        } else {
            SecureRandom()
        }
    }

    private val bytesData = MutableLiveData<Message?>()
    val randomBytes get() = bytesData as LiveData<Message?>
    private var getBytesJob: Job? = null
    var secureBytes: ByteArray? = null

    fun clearMessage(){
        bytesData.value = null
    }

    class Message(
        val state: Int,
        val msg: Int = 0,
    )

    fun getBytes(count: Int){
        if(getBytesJob.isRunning()){
            bytesData.postValue(Message(msg = R.string.secret_key_gen_already_running, state = MSG))
            return
        }
        getBytesJob = viewModel.viewModelScope.launch(Dispatchers.Default) {
            bytesData.postValue(Message(state = START))
            val bytes = ByteArray(count)
            random.nextBytes(bytes)
            secureBytes = bytes
            bytesData.postValue(Message(state = END))
        }
    }
}