package hu.mcold.gordian.network

import haart.bme.gordian.hu.mcold.gordian.network.StatusLogger
import kotlinx.coroutines.*
import java.net.Socket

open class TimeOutJob(private val scope: CoroutineScope) {
    var job: Job? = null
    operator fun invoke(socket: Socket, timeout: Long, logger: StatusLogger){
        job = scope.launch(Dispatchers.IO) {
            delay(timeout.toLong())
            socket.close()
            timeOutReached(logger)
        }
    }

    open fun timeOutReached(logger: StatusLogger){
        logger.postError("Connection timed out")
    }
}

class AuthTimeOutJob(s: CoroutineScope, private val logger: StatusLogger): TimeOutJob(s){
    override fun timeOutReached(logger: StatusLogger) {
        this.logger.postError("Connection timed out")
    }
}
