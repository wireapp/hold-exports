package com.wire.integrations.hold.exports.utils

import org.kodein.di.DI

open class DiAwareTest {
    protected val di by lazy {
        DI(allowSilentOverride = true) {
            extend(com.wire.integrations.hold.exports.di)
            additionalModules()
        }
    }

    protected var additionalModules: DI.MainBuilder.() -> Unit = {}
}
