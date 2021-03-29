package com.wire.integrations.hold.exports.api

import com.wire.helium.API
import com.wire.xenon.WireAPI

/**
 * Wrapper around [API].
 */
class WireApi(api: API) : WireAPI by api
