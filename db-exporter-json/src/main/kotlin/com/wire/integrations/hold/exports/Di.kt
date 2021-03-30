package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.dao.RawEventsRepository
import com.wire.integrations.hold.exports.dto.DatabaseConfiguration
import com.wire.integrations.hold.exports.service.ExporterService
import com.wire.integrations.hold.exports.service.ProcessingService
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.singleton
import pw.forst.tools.katlib.getEnv

val di = DI {
    import(CommonDi.module)

    bind<Int>("executor-cores") with singleton {
        getEnv("EXECUTOR_CORES")?.toInt() ?: 1
    }

    bind<Long>("executor-check-millis") with singleton {
        getEnv("EXECUTOR_CHECK_MILLIS")?.toLong() ?: 60_000 // 1 minute
    }

    bind<Long>("executor-tasks-seconds") with singleton {
        getEnv("EXECUTOR_TASKS_SECONDS")?.toLong() ?: 60
    }

    bind<Int>("events-batch-size") with singleton {
        getEnv("EVENTS_BATCH_SIZE")?.toInt() ?: 500
    }

    bind<ExecutorLoop>() with provider {
        ExecutorLoop(corePoolSize = instance("executor-cores"))
    }

    bind<DatabaseConfiguration>() with singleton {
        val db = getEnv("POSTGRES_DB") ?: "hold"
        val dbHost = getEnv("POSTGRES_HOST") ?: "localhost:5432"

        DatabaseConfiguration(
            userName = getEnv("POSTGRES_USER") ?: "wire",
            password = getEnv("POSTGRES_PASSWORD") ?: "wire-password",
            url = "jdbc:postgresql://${dbHost}/${db}"
        )
    }

    bind<RawEventsRepository>() with singleton {
        RawEventsRepository(instance(), batchSize = instance("events-batch-size"))
    }

    bind<Exporter>() with singleton { instance<ConsoleLogExporter>() }

    bind<ProcessingService>() with singleton { ProcessingService(instance(), instance(), instance()) }
    bind<ExporterService>() with singleton { ExporterService(instance(), instance(), instance()) }
}
