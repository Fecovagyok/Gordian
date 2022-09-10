package com.example.szakchat.extensions

import android.widget.EditText

fun EditText.isEmpty(): Boolean {
    return text == null || text.toString().isEmpty()
}

fun String.isBadText() = contains('\n') || isEmpty()