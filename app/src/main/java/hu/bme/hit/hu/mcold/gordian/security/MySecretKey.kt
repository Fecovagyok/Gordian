package hu.bme.gordian.hu.mcold.gordian.security

import com.example.szakchat.common.MyByteArray
import javax.crypto.SecretKey

class MySecretKey(values: ByteArray) : MyByteArray(values), SecretKey{
    private var isDestroyed = false
    private var canBeDestroyed = false
    override fun getAlgorithm(): String {
        return "AES_GCM"
    }

    override fun getFormat(): String = "RAW"


    override fun getEncoded(): ByteArray {
        return values
    }

    override fun isDestroyed(): Boolean {
        return isDestroyed
    }

    fun setCanBeDestroyed(value: Boolean, provider: MyKeyProvider){
        canBeDestroyed = value
    }

    override fun destroy() {
        if(!canBeDestroyed)
            return
        isDestroyed = true
        values.fill(0)
    }
}