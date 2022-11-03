package com.example.szakchat.security

import android.util.Log
import com.example.szakchat.common.MSG_VERSION
import com.example.szakchat.common.TYPE_HELLO
import com.example.szakchat.common.TYPE_MESSAGE
import com.example.szakchat.exceptions.ProtocolException
import com.example.szakchat.exceptions.TooLongMessage
import com.example.szakchat.extensions.*
import com.example.szakchat.identity.UserID
import com.example.szakchat.messages.Message
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec


class MySecurityProtocol(private val random: SecureRandom){
    companion object {
        const val GCM_HEADER_LENGTH = 33 + 8 // = 41
        const val TAG_LENGTH = 128 // in bytes
        const val RND_LENGTH = 8
    }


    private fun aadOf(
        type: Int,
        len: Int,
        seqNum: Int,
        rnd: ByteArray,
        date: Long,
        src: UserID,
        dst: UserID,
    ): ByteArray {
        val array = ByteArray(GCM_HEADER_LENGTH)
        val seq = sequenceOf(
            MSG_VERSION.toSecondMostByte(), MSG_VERSION.toByte(),
            type.toByte(),
            len.toSecondMostByte(), len.toByte(),
            seqNum.toLastMostByte(), seqNum.toThirdMostByte(), seqNum.toSecondMostByte(), seqNum.toByte(),
        )
        seq.forEachIndexed { index, byte ->
            array[index] = byte
        }
        System.arraycopy(rnd, 0, array, 9, rnd.size)
        System.arraycopy(src.values, 0, array, 17, src.values.size)
        System.arraycopy(dst.values, 0, array, 25, dst.values.size)
        val dateArray = date.toByteArray()
        System.arraycopy(dateArray, 0, array, 33, dateArray.size)
        return array
    }

    private fun ivOf(seqNum: Int, rnd: ByteArray): ByteArray {
        val bytes = ByteArray(12)
        seqNum.copyBytes(bytes)
        System.arraycopy(rnd, 0, bytes, 4, rnd.size)
        bytes[11] = rnd[rnd.lastIndex]
        return bytes
    }

    fun decode(
        keyProvider: ReceiverKeyProvider,
        gcmMessage: GcmMessage,
    ): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = keyProvider.getKeyUntil(gcmMessage.seqNum)
        val aad = aadOf(
            type = gcmMessage.type,
            len = gcmMessage.length,
            seqNum = gcmMessage.seqNum,
            rnd = gcmMessage.rnd.values,
            date = gcmMessage.date,
            src = gcmMessage.src,
            dst = gcmMessage.dst
        )
        val spec = GCMParameterSpec(TAG_LENGTH*8, aad, 5, 12)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        cipher.updateAAD(aad)
        val text = cipher.doFinal(gcmMessage.ciphered.values, 0, gcmMessage.length)
            .toString(Charsets.UTF_8)
        return text
    }

    fun encode(
        message: Message,
        keyProvider: SenderKeyProvider = message.contact.keys!!.sender,
        type: Int = TYPE_MESSAGE,
    ): GcmMessage {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val plainTextBytes = message.text.toByteArray(Charsets.UTF_8)
        if(plainTextBytes.size > Short.MAX_VALUE-16)
            throw TooLongMessage()
        val key = keyProvider.nextKey()
        val rnd = random.nextAndCreateBytes(RND_LENGTH)
        val iv = ivOf(keyProvider.sequenceNumber, rnd)
        val spec = GCMParameterSpec(TAG_LENGTH*8, iv)

        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        val len = cipher.getOutputSize(plainTextBytes.size)
        Log.d("FECO", "Bytes size: ${plainTextBytes.size} cipher size: $len Difference: ${len-plainTextBytes.size}")
        val myAAD = aadOf(
            type = type,
            len = len,
            rnd = rnd,
            seqNum = keyProvider.sequenceNumber,
            date = message.date,
            src = message.owner,
            dst = message.contact.uniqueId!!,
        )
        cipher.updateAAD(myAAD)
        val outBytes = ByteArray(len)
        val outLen = cipher.doFinal(plainTextBytes, 0, plainTextBytes.size, outBytes)
        if(outLen != len)
            throw IllegalStateException("AES_GCM output is not what expected")

        return GcmMessage(
            version = MSG_VERSION,
            type = type,
            length = len,
            seqNum = keyProvider.sequenceNumber,
            rnd = rnd.toMyByteArray(),
            date = message.date,
            src = message.owner,
            dst = message.contact.uniqueId,
            ciphered = outBytes.toMyByteArray(),
        )
    }

    private fun gcmSpecOf(keyProvider: MyKeyProvider, rnd: ByteArray): GCMParameterSpec {
        val iv = ivOf(keyProvider.sequenceNumber, rnd)
        return GCMParameterSpec(TAG_LENGTH*8, iv)
    }

    fun craftHelloMessage(keyProvider: MyKeyProvider, id: UserID, owner: UserID): GcmMessage {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = keyProvider.lastKey
        val rnd = random.nextAndCreateBytes(RND_LENGTH)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpecOf(keyProvider, rnd))
        val len = cipher.getOutputSize(0)
        val date = System.currentTimeMillis()
        val myAAD = aadOf(
            type = TYPE_HELLO,
            len = len,
            seqNum = keyProvider.sequenceNumber,
            rnd = rnd,
            date = date,
            src = owner,
            dst = id,
        )
        cipher.updateAAD(myAAD)
        val outBytes = ByteArray(len)
        val outLen = cipher.doFinal(ByteArray(0), 0, 0, outBytes)
        if(outLen != len)
            throw IllegalStateException("AES_GCM output is not what expected")

        return GcmMessage(
            version = MSG_VERSION,
            type = TYPE_HELLO,
            length = len,
            seqNum = keyProvider.sequenceNumber,
            rnd = rnd.toMyByteArray(),
            src = owner,
            dst = id,
            date = date,
            ciphered = outBytes.toMyByteArray()
        )
    }

    fun decodeHelloMessage(gcmMessage: GcmMessage, keyProvider: ReceiverKeyProvider) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = keyProvider.lastKey
        val aad = aadOf(
            type = gcmMessage.type,
            len = gcmMessage.length,
            seqNum = gcmMessage.seqNum,
            rnd = gcmMessage.rnd.values,
            date = gcmMessage.date,
            src = gcmMessage.src,
            dst = gcmMessage.dst
        )
        val spec = GCMParameterSpec(TAG_LENGTH*8, aad, 5, 12)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        cipher.updateAAD(aad)
        val text = cipher.doFinal(gcmMessage.ciphered.values, 0, gcmMessage.length)
        if(text.isNotEmpty())
            throw ProtocolException("Payload empty while decoding hello message")
    }

}