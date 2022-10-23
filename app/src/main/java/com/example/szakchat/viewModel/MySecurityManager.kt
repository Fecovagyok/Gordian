package com.example.szakchat.viewModel

import android.os.Build
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.szakchat.R
import com.example.szakchat.extensions.isRunning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.security.MessageDigest
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

    private val bytesData = MutableLiveData<StatusMessage?>()
    val randomBytes get() = bytesData as LiveData<StatusMessage?>
    private var getBytesJob: Job? = null
    var secureBytes: ByteArray? = null
    val secureString: String? get() = secureBytes?.let {
        Base64.encodeToString(it, Base64.DEFAULT)
    }
    val secureSha get() = secureBytes?.let {
        MessageDigest.getInstance("SHA-256").digest(it)
    }

    fun setSecureBytes(msg: String) {
        secureBytes = Base64.decode(msg, Base64.DEFAULT)
    }

    fun clearMessage(){
        bytesData.value = null
    }

    class StatusMessage(
        val state: Int,
        val msg: Int = 0,
    )

    fun getBytesAsync(count: Int){
        if(getBytesJob.isRunning()){
            bytesData.postValue(StatusMessage(msg = R.string.secret_key_gen_already_running, state = MSG))
            return
        }
        getBytesJob = viewModel.viewModelScope.launch(Dispatchers.Default) {
            bytesData.postValue(StatusMessage(state = START))
            val bytes = ByteArray(count)
            random.nextBytes(bytes)
            secureBytes = bytes
            bytesData.postValue(StatusMessage(state = END))
        }
    }
}