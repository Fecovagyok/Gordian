package com.example.szakchat.viewModel

import android.os.Build
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.szakchat.R
import com.example.szakchat.extensions.isRunning
import com.example.szakchat.security.MySecurityProtocol
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

    private val randomObject: SecureRandom by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SecureRandom.getInstanceStrong()
        } else {
            SecureRandom()
        }
    }

    val myProto = MySecurityProtocol(randomObject)

    private val _liveRandomBytes = MutableLiveData<StatusMessage?>()
    val liveRandomBytes get() = _liveRandomBytes as LiveData<StatusMessage?>
    private var getBytesJob: Job? = null
    var generatedLotsOfBytes: ByteArray? = null
        private set
    val secureString: String? get() = generatedLotsOfBytes?.let {
        Base64.encodeToString(it, Base64.DEFAULT)
    }

    fun setSecureBytes(msg: String) {
        generatedLotsOfBytes = Base64.decode(msg, Base64.DEFAULT)
    }

    fun clearMessage(){
        _liveRandomBytes.value = null
    }

    class StatusMessage(
        val state: Int,
        val msg: Int = 0,
    )

    fun getBytesAsync(count: Int){
        val countWithOutId = count-8
        if(getBytesJob.isRunning()){
            _liveRandomBytes.postValue(StatusMessage(msg = R.string.secret_key_gen_already_running, state = MSG))
            return
        }
        getBytesJob = viewModel.viewModelScope.launch(Dispatchers.Default) {
            _liveRandomBytes.postValue(StatusMessage(state = START))
            val bytes = ByteArray(countWithOutId)
            randomObject.nextBytes(bytes)
            val bytesWithID = ByteArray(count)
            bytes.copyInto(bytesWithID)
            viewModel.networking.self!!.values.copyInto(bytesWithID, countWithOutId,)
            generatedLotsOfBytes = bytesWithID
            _liveRandomBytes.postValue(StatusMessage(state = END))
        }
    }
}