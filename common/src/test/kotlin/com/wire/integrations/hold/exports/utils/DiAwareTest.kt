package com.wire.integrations.hold.exports.utils

import com.wire.integrations.hold.exports.CommonDi
import org.kodein.di.DI

open class DiAwareTest {
    protected val di by lazy {
        DI(allowSilentOverride = true) {
            import(CommonDi.module)
            additionalModules()
        }
    }

    protected var additionalModules: DI.MainBuilder.() -> Unit = {}
}
