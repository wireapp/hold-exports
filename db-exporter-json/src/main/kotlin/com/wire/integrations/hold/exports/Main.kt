package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.dao.DatabaseSetup
import com.wire.integrations.hold.exports.dto.DatabaseConfiguration
import com.wire.integrations.hold.exports.service.ExporterService
import com.wire.integrations.hold.exports.utils.createLogger
import org.kodein.di.DI
import org.kodein.di.instance
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


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
    val delaySeconds by di.instance<Long>("executor-tasks-seconds")
    val executorCheckTime by di.instance<Long>("executor-check-millis")

    val executor by di.instance<ExecutorLoop>()
    val exporterService by di.instance<ExporterService>()

    runCatching {
        // schedule task
        executor.schedule(seconds = delaySeconds, name = "export-task") {
            exporterService.executeExports()
        }
        // and periodically check if the executor is alive
        while (!executor.isShutdown && !executor.isTerminated) {
            sleep(executorCheckTime)
        }
    }.onFailure {
        logger.error { "Executor loop failed, terminating thread pool." }
        executor.awaitTermination(delaySeconds, TimeUnit.MINUTES)
        logger.warn { "Pool terminated." }
    }

    logger.warn { "Executor loop exited, trying again." }
    // if not, execute the loop again
    runExecutor(di, attemptCounts + 1)
}

private val logger = createLogger("Main")
