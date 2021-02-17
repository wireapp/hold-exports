package com.wire.integrations.hold.exports.utils

/**
 * Simple configuration for the database connection.
 */
data class DatabaseConfiguration(
    /**
     * Username for login.
     */
    val userName: String,
    /**
     * Password for login.
     */
    val password: String,
    /**
     * URL where the database is running.
     */
    val url: String
)
