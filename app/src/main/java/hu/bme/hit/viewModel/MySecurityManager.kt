package hu.bme.hit.viewModel

import android.os.Build
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.szakchat.R
import com.example.szakchat.common.StatusMessage
import com.example.szakchat.exceptions.ProtocolException
import com.example.szakchat.extensions.isRunning
import com.example.szakchat.extensions.toMySecretKey
import com.example.szakchat.extensions.toUserID
import com.example.szakchat.identity.UserID
import com.example.szakchat.security.*
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


    private fun createBiggerArrays(randomSize: Int, bytes: ByteArray): ByteArray{
        val abBytes = ByteArray(randomSize+2)
        bytes.copyInto(abBytes, endIndex = randomSize)
        return abBytes
    }

    private inline fun<T: MyKeyProvider> ByteArray.toMyKeyProvider(
        createKeyProvider: (MySecretKey, Int) -> T
    ): T {
        val shaObject = MessageDigest.getInstance("SHA-256")
        val key = shaObject.digest(this).toMySecretKey()
        return createKeyProvider(key, 0)
    }

    private fun processGeneratedBytes(bytes: ByteArray, randomSize: Int, senderCode: Char, receiverCode: Char): KeyProviders {

        val senderBytes = createBiggerArrays(randomSize, bytes)
        senderBytes[randomSize] = senderCode.code.toByte()
        senderBytes[randomSize+1] = receiverCode.code.toByte()
        val receiverBytes = createBiggerArrays(randomSize, bytes)
        receiverBytes[randomSize] = receiverCode.code.toByte()
        receiverBytes[randomSize+1] = senderCode.code.toByte()

        return KeyProviders(
            sender = senderBytes.toMyKeyProvider { mySecretKey, i -> SenderKeyProvider(mySecretKey, i) },
            receiver = receiverBytes.toMyKeyProvider { key, num -> ReceiverKeyProvider(key, num) }
        )
    }

    fun processQrDataAsShower(bytes: ByteArray): KeyProviders {
        if(bytes.size != 1024)
            throw ProtocolException("Not enough secret bytes generated")
        return processGeneratedBytes(bytes, 1024-8, 'A', 'B')
    }

    fun processQrDataAsReader(bytes: ByteArray): Pair<UserID, KeyProviders> {
        if(bytes.size != 1024)
            throw ProtocolException("Not enough secret bytes through QR code")
        val randomSize = 1024-8

        val keys = processGeneratedBytes(bytes, randomSize, 'B', 'A')

        val id = bytes.sliceArray(randomSize until 1024).toUserID()
        if(id.size != 8)
            throw IllegalStateException("UserID must be exactly 8 bytes, got: ${id.size}")
        return id to keys
    }

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