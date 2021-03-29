package com.wire.integrations.hold.exports.dto

/**
 * Credentials for any user that can log in to Wire.
 */
data class WireCredentials(
    val email: String,
    val password: String
)
