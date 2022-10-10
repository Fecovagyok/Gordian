package com.example.szakchat.extensions

import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job

fun EditText.isEmpty(): Boolean {
    return text == null || text.toString().isEmpty()
}

fun String.isBadText() = contains('\n') || isEmpty()

fun RecyclerView.scrollToTheEnd() {
    Log.d("FECO", "AdapterItemCount: ${adapter!!.itemCount}")
    scrollToPosition(adapter!!.itemCount-1)
}

fun Job?.isRunning() = this?.isCompleted == false

fun View.postSnack(@StringRes msg: Int) = Snackbar.make(this, msg, Snackbar.LENGTH_LONG).show()