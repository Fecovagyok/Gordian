package hu.bme.hit.hu.mcold.gordian.common

import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hu.bme.hit.hu.mcold.gordian.login.UserID
import hu.bme.hit.hu.mcold.gordian.security.MySecretKey
import kotlinx.coroutines.Job
import java.net.Socket
import java.security.SecureRandom
import javax.net.ssl.SSLProtocolException

fun EditText.isEmpty(): Boolean {
    return text == null || text.toString().isEmpty()
}

fun EditText.moreThan(num: Int) = text.length > num

fun String.isBadText() = trim().isEmpty()

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
fun ByteArray.toMySecretKey() = MySecretKey(this)
fun ByteArray.toUserID(): UserID {
    /*if(size != 8)
        throw IllegalStateException("UserID must be exactly 8 bytes, given: $size")*/
    return UserID(this)
}
fun MyByteArray.toHex() = values.toHex()
fun Long.toHex() = toByteArray().toHex()
fun Int.toHex() = toByteArray().toHex()
fun Byte.toByteArray() = ByteArray(1) { this }
fun Byte.toHex() = toByteArray().toHex()
fun String.toData(): ByteArray = Base64.decode(this, Base64.DEFAULT)
fun ByteArray.toBase64String(): String = Base64.encodeToString(this, Base64.DEFAULT)
fun String.toUserID() = UserID(Base64.decode(this, Base64.DEFAULT))
fun Int.copyBytes(dest: ByteArray){
    dest[0] = toLastMostByte()
    dest[1] = toThirdMostByte()
    dest[2] = toSecondMostByte()
    dest[3] = toByte()
}

fun Int.myToLong() = toLong() and 0x00000000ffffffff

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
    copyToBytes(bytes)
    return bytes
}

fun Int.copyToBytes(bytes: ByteArray){
    bytes[0] = toLastMostByte()
    bytes[1] = toThirdMostByte()
    bytes[2] = toSecondMostByte()
    bytes[3] = toByte()
}

fun Int.copyToBytes(bytes: ByteArray, offset: Int){
    bytes[0+offset] = toLastMostByte()
    bytes[1+offset] = toThirdMostByte()
    bytes[2+ offset] = toSecondMostByte()
    bytes[3+offset] = toByte()
}

fun Long.toByteArray(): ByteArray {
    val bytes = ByteArray(8)
    (this ushr 32).toInt().copyToBytes(bytes)
    toInt().copyToBytes(bytes, 4)
    return bytes
}

fun SecureRandom.nextAndCreateBytes(num: Int): ByteArray {
    val bytes = ByteArray(num)
    nextBytes(bytes)
    return bytes
}

fun Socket.awaitClose() {
    try {
        Log.d("FECO", "Waiting for the peer to close the socket")
        getInputStream().read()
    } catch (e: SSLProtocolException){
        Log.d("FECO", "Socket closed when I wanted it to")
    }
}