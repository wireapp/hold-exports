package com.wire.integrations.hold.exports

import mu.KLogging
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ExecutorLoop(
    private val corePoolSize: Int = 1,
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(corePoolSize)
) {

    private companion object : KLogging()

    /**
     * Schedule given task [name] to repeat each time interval - executes [scheduledTask].
     *
     * Note - use just a single parameter from [seconds], [minutes], [hours].
     */
    fun schedule(
        seconds: Long? = null,
        minutes: Long? = null,
        hours: Long? = null,
        name: String = "default",
        scheduledTask: () -> Unit
    ) {
        val (delay, timeInterval) = when {
            seconds != null -> seconds to TimeUnit.SECONDS
            minutes != null -> minutes to TimeUnit.MINUTES
            hours != null -> hours to TimeUnit.HOURS
            else -> throw IllegalArgumentException("No time interval set!")
        }

        schedule(delay, timeInterval, name, scheduledTask)
    }

    private fun schedule(delay: Long, unit: TimeUnit, taskName: String, scheduledTask: () -> Unit) {
        logger.debug { "Adding task \"$taskName\" to execution pool with delay $delay $unit." }
        executor.scheduleWithFixedDelay({
            logger.debug { "Executing scheduled \"$taskName\" task." }
            runCatching(scheduledTask)
                .onFailure { logger.error(it) { "Task \"$taskName\" failed with exception" } }
                .onSuccess { logger.debug { "Task \"$taskName\" executed successfully." } }
        }, 0, delay, unit)
    }
}
