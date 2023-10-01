package club.maxstats.commission.stathead.util

import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Async {
    private val executor = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat("czbczstathead-%d").build())
    private val scheduledExecutor = ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1)
    fun <T> async(callable: () -> T): Future<T> =
        executor.submit(callable)
    fun <T> schedule(callable: () -> T, delay: Long, timeUnit: TimeUnit): ScheduledFuture<T> =
        scheduledExecutor.schedule(callable, delay, timeUnit)
}