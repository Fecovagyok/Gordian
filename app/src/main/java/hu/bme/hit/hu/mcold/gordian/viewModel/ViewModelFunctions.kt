package hu.bme.hit.hu.mcold.gordian.viewModel

import hu.bme.hit.hu.mcold.gordian.network.TimeOutJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin

internal suspend inline fun <T> withTimeOut(coroutineScope: CoroutineScope, block: (TimeOutJob) -> T): T{
    val timeOutJob = TimeOutJob(coroutineScope)
    return withTimeOut(timeOutJob, block)
}

internal suspend inline fun <T> withTimeOut(timeOutJob: TimeOutJob, block: (TimeOutJob) -> T): T{
    val result = block(timeOutJob)
    timeOutJob.job?.cancelAndJoin()
    return result
}


