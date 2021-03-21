package com.wire.integrations.hold.exports

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wire.helium.API
import com.wire.helium.LoginClient
import com.wire.integrations.hold.exports.convert.AssetsDownloader
import com.wire.integrations.hold.exports.convert.AssetsFileDownloader
import com.wire.integrations.hold.exports.convert.EventParser
import com.wire.integrations.hold.exports.dao.RawEventsRepository
import com.wire.integrations.hold.exports.dto.DatabaseConfiguration
import com.wire.integrations.hold.exports.dto.WireCredentials
import com.wire.integrations.hold.exports.utils.JerseyClientBuilder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.singleton
import pw.forst.tools.katlib.InstantTimeProvider
import pw.forst.tools.katlib.TimeProvider
import pw.forst.tools.katlib.getEnv
import java.time.Instant
import javax.ws.rs.client.Client

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

    // TODO maybe proxy will be needed
    bind<Client>() with singleton { JerseyClientBuilder.build(); }

    bind<WireCredentials>() with singleton {
        // TODO consider loading this from properties
        WireCredentials(
            email = getEnv("WIRE_EMAIL") ?: "wire",
            password = getEnv("WIRE_PASSWORD") ?: "test"
        )
    }

    bind<LoginClient>() with singleton { LoginClient(instance()) }

    bind<API>() with provider {
        val credentials = instance<WireCredentials>()
        val loginClient = instance<LoginClient>()
        val access = loginClient.login(credentials.email, credentials.password, false)
        API(instance(), null, access.getAccessToken())
    }

    bind<AssetsDownloader>() with singleton { AssetsDownloader(provider()) }
    bind<AssetsFileDownloader>() with singleton { AssetsFileDownloader(instance()) }
}
