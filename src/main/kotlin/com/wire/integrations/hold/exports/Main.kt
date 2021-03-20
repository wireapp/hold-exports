package com.wire.integrations.hold.exports

import org.kodein.di.instance

fun main() {
    val executor by di.instance<ExecutorLoop>()

    executor
        .schedule(seconds = 30, name = "hello-task") {
            println("Hello world")
        }
    // we can leave this thread to terminate
}
