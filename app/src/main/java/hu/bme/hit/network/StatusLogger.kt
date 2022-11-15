package hu.bme.hit.network

interface StatusLogger {
    fun postError(msg: String)
    fun postMessage(msg: String)
}