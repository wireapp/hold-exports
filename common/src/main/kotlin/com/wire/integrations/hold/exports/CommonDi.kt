package com.wire.integrations.hold.exports

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wire.helium.API
import com.wire.helium.LoginClient
import com.wire.integrations.hold.exports.api.WireApi
import com.wire.integrations.hold.exports.asset.AssetsDownloader
import com.wire.integrations.hold.exports.asset.AssetsFileDownloader
import com.wire.integrations.hold.exports.convert.EventParser
import com.wire.integrations.hold.exports.dto.ApplicationInfo
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
import java.io.File
import java.time.Instant
import javax.ws.rs.client.Client

/**
 * DI with reasonable defaults for common module.
 */
object CommonDi {
    /**
     * Build static module, lazy creation because sometimes it won't be needed.
     */
    val module by lazy {
        DI.Module("common") {
            bind<ApplicationInfo>() with singleton {
                val version = getEnv("RELEASE_FILE_PATH")
                    ?.let { runCatching { File(it).readText() }.getOrNull() }
                    ?.trim()
                    ?: "development"
                ApplicationInfo(version)
            }

            bind<TimeProvider<Instant>>() with singleton { InstantTimeProvider }

            bind<ObjectMapper>() with singleton {
                jacksonObjectMapper().apply {
                    registerModule(JavaTimeModule())

                    configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                    configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false)
                }
            }

            bind<EventParser>() with singleton { EventParser(instance()) }

            // TODO maybe proxy will be needed
            bind<Client>() with singleton { JerseyClientBuilder.build(); }

            // TODO maybe allow loading this as program arguments / properties file
            bind<WireCredentials>() with singleton {
                WireCredentials(
                    email = requireNotNull(getEnv("WIRE_EMAIL")) { "Environmental variable WIRE_EMAIL not set!" },
                    password = requireNotNull(getEnv("WIRE_PASSWORD")) { "Environmental variable WIRE_PASSWORD not set!" }
                )
            }

            bind<LoginClient>() with singleton { LoginClient(instance()) }

            // Creates API instance that is logged in.
            bind<API>() with provider {
                val credentials = instance<WireCredentials>()
                val loginClient = instance<LoginClient>()
                val access = loginClient.login(credentials.email, credentials.password, false)
                API(instance(), null, access.getAccessToken())
            }
            bind<WireApi>() with provider {
                WireApi(provider<API>().invoke())
            }

            bind<AssetsDownloader>() with singleton { AssetsDownloader(provider()) }
            bind<AssetsFileDownloader>() with singleton { AssetsFileDownloader(instance()) }

            bind<ConsoleLogExporter>() with singleton { ConsoleLogExporter() }
        }
    }
}
