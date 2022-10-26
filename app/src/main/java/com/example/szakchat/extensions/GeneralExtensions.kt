package com.example.szakchat.extensions

import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.example.szakchat.identity.MyByteArray
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job

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
fun MyByteArray.toHex() = values.toHex()
fun String.toData() = Base64.decode(this, Base64.DEFAULT)