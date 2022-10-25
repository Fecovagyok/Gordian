package com.example.szakchat.network

interface StatusLogger {
    fun postError(msg: String)
    fun postMessage(msg: String)
}