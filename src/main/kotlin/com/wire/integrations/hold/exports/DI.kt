package com.wire.integrations.hold.exports

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wire.integrations.hold.exports.convert.EventParser
import com.wire.integrations.hold.exports.dao.RawEventsRepository
import com.wire.integrations.hold.exports.dto.DatabaseConfiguration
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import pw.forst.tools.katlib.InstantTimeProvider
import pw.forst.tools.katlib.TimeProvider
import pw.forst.tools.katlib.getEnv
import java.time.Instant

val di = DI {
    bind<Int>("executor-cores") with singleton {
        getEnv("EXECUTOR_CORES")?.toInt() ?: 1
    }

    bind<ExecutorLoop>() with singleton {
        ExecutorLoop(corePoolSize = instance("executor-cores"))
    }

    bind<ObjectMapper>() with singleton {
        jacksonObjectMapper().apply {
            registerModule(JavaTimeModule())

            configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false)
        }
    }

    bind<DatabaseConfiguration>() with singleton {
        DatabaseConfiguration(
            userName = getEnv("POSTGRES_USER") ?: "wire",
            password = getEnv("POSTGRES_PASSWORD") ?: "wire-password",
            url = getEnv("POSTGRES_URL") ?: "localhost:5432/hold"
        )
    }
    bind<TimeProvider<Instant>>() with singleton { InstantTimeProvider }

    bind<RawEventsRepository>() with singleton { RawEventsRepository(instance()) }

    bind<EventParser>() with singleton { EventParser(instance()) }
}
