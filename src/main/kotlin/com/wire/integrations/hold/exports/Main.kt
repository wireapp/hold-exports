package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.dao.DatabaseSetup
import com.wire.integrations.hold.exports.dto.DatabaseConfiguration
import com.wire.integrations.hold.exports.utils.createLogger
import org.kodein.di.DI
import org.kodein.di.instance
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

val logger = createLogger("Main")

fun main() {
    logger.info { "Initializing application." }
    val di = di

    logger.info { "Connecting to the database." }
    val databaseConfiguration by di.instance<DatabaseConfiguration>()
    DatabaseSetup.connect(databaseConfiguration)

    if (!DatabaseSetup.isConnected()) {
        logger.error { "It was not possible to connect to the database!" }
        exitProcess(1)
    }

    logger.info { "Starting the executor." }
    // create infinite loop that will check the executor once per some time
    // if it is not running, it wil start it up
    runExecutor(di)
}

// TODO maybe implement some sort of graceful shutdown mechanism
private tailrec fun runExecutor(di: DI, attemptCounts: Int = 1) {
    logger.info { "Starting executor, attempt #${attemptCounts}." }
    val delayMinutes by di.instance<Long>("executor-tasks-minutes")
    val executorCheckTime by di.instance<Long>("executor-check-millis")

    val executor by di.instance<ExecutorLoop>()

    runCatching {
        // schedule task
        executor.schedule(minutes = delayMinutes, name = "hello-task") {
            println("Hello world")
        }
        // and periodically check if the executor is alive
        while (!executor.isShutdown && !executor.isTerminated) {
            sleep(executorCheckTime)
        }
    }.onFailure {
        logger.error { "Executor loop failed, terminating thread pool." }
        executor.awaitTermination(delayMinutes, TimeUnit.MINUTES)
        logger.warn { "Pool terminated." }
    }

    logger.warn { "Executor loop exited, trying again." }
    // if not, execute the loop again
    runExecutor(di, attemptCounts + 1)
}
