package com.wire.integrations.hold.exports

fun main() {
    ExecutorLoop()
        .scheduleRunnableForMinutes(1) {
            println("Hello world")
        }
    // we can leave this thread to terminate
}
