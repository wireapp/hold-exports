package com.wire.integrations.hold.exports

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val di = DI {
    import(CommonDi.module)
    bind<Exporter>() with singleton { instance<ConsoleLogExporter>() }
    bind<ProcessingService>() with singleton { ProcessingService(instance(), instance()) }
}
