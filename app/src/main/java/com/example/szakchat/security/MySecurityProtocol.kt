package com.example.szakchat.security

import com.example.szakchat.exceptions.TooLongMessage
import com.example.szakchat.extensions.*
import com.example.szakchat.identity.UserID
import com.example.szakchat.messages.Message
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec


class MySecurityProtocol(private val random: SecureRandom){
    companion object {
        const val TYPE_MESSAGE = 1
        const val MSG_VERSION = 1
        const val GCM_HEADER_LENGTH = 33
        const val TAG_LENGTH = 128 // in bytes
    }

    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")

    private fun aadOf(
        type: Int,
        len: Int,
        seqNum: Int,
        rnd: ByteArray,
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
        array[16] = rnd.last()
        System.arraycopy(src.values, 0, array, 17, src.values.size)
        System.arraycopy(dst.values, 0, array, 25, dst.values.size)
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
        val key = keyProvider.getKeyUntil(gcmMessage.seqNum)
        val aad = aadOf(
            type = gcmMessage.type,
            len = gcmMessage.length,
            seqNum = gcmMessage.seqNum,
            rnd = gcmMessage.rnd.values,
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
        val plainTextBytes = message.text.toByteArray(Charsets.UTF_8)
        if(plainTextBytes.size > Short.MAX_VALUE)
            throw TooLongMessage()
        val key = keyProvider.nextKey()
        val rnd = random.nextAndCreateBytes(7)
        val iv = ivOf(keyProvider.sequenceNumber, rnd)
        val spec = GCMParameterSpec(TAG_LENGTH*8, iv)

        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        val len = cipher.getOutputSize(plainTextBytes.size)
        val myAAD = aadOf(
            type = type,
            len = len,
            rnd = rnd,
            seqNum = keyProvider.sequenceNumber,
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
            src = message.owner,
            dst = message.contact.uniqueId,
            ciphered = outBytes.toMyByteArray(),
        )
    }
}