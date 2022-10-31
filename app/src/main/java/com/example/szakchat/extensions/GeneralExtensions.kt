package com.example.szakchat.extensions

import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.example.szakchat.common.MyByteArray
import com.example.szakchat.identity.UserID
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import java.security.SecureRandom

fun EditText.isEmpty(): Boolean {
    return text == null || text.toString().isEmpty()
}

fun EditText.moreThan(num: Int) = text.length > num

fun String.isBadText() = contains('\n') || isEmpty()

fun RecyclerView.scrollToTheEnd() {
    Log.d("FECO", "AdapterItemCount: ${adapter!!.itemCount}")
    scrollToPosition(adapter!!.itemCount-1)
}

fun Job?.isRunning() = this?.isCompleted == false

fun View.postSnack(@StringRes msg: Int) = Snackbar.make(this, msg, Snackbar.LENGTH_LONG).show()

fun ByteArray.toHex(): String {
    val hexString = StringBuilder(2 * size)
    for (i in indices) {
        val hex = Integer.toHexString(0xff and this[i].toInt())
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }
    return hexString.toString()
}

fun ByteArray.toMyByteArray() = MyByteArray(this)
fun ByteArray.toUserID() = UserID(this)
fun MyByteArray.toHex() = values.toHex()
fun String.toData() = Base64.decode(this, Base64.DEFAULT)
fun ByteArray.toBase64String() = Base64.encodeToString(this, Base64.DEFAULT)
fun String.toUserID() = UserID(Base64.decode(this, Base64.DEFAULT))
fun Int.copyBytes(dest: ByteArray){
    dest[0] = toLastMostByte()
    dest[1] = toThirdMostByte()
    dest[2] = toSecondMostByte()
    dest[3] = toByte()
}

fun Short.toSecondMostByte(): Byte {
    val target = toInt() ushr 8
    return target.toByte()
}

fun Int.toSecondMostByte(): Byte {
    val target = this ushr 8
    return target.toByte()
}

fun Int.toThirdMostByte(): Byte {
    val target = this ushr 16
    return target.toByte()
}

fun Int.toLastMostByte(): Byte {
    val target = this ushr 24
    return target.toByte()
}

fun Int.toByteArray(): ByteArray {
    val bytes = ByteArray(4)
    bytes[0] = toByte()
    bytes[1] = toSecondMostByte()
    bytes[2] = toThirdMostByte()
    bytes[3] = toLastMostByte()
    return bytes
}

fun SecureRandom.nextAndCreateBytes(num: Int): ByteArray {
    val bytes = ByteArray(num)
    nextBytes(bytes)
    return bytes
}