package com.example.szakchat.extensions

import android.util.Log
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

fun EditText.isEmpty(): Boolean {
    return text == null || text.toString().isEmpty()
}

fun String.isBadText() = contains('\n') || isEmpty()

fun RecyclerView.scrollToTheEnd() {
    Log.d("FECO", "AdapterItemCount: ${adapter!!.itemCount}")
    scrollToPosition(adapter!!.itemCount-1)
}