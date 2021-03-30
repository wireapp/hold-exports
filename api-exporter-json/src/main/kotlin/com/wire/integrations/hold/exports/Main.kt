package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.server.init
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import pw.forst.tools.katlib.getEnv

fun main() {
    val port = getEnv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port, module = Application::init).start()
}
