package hu.mcold.gordian.network

interface StatusLogger {
    fun postError(msg: String)
    fun postMessage(msg: String)
}