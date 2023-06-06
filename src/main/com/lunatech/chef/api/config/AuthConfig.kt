package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class AuthConfig(
    val secretKey: String,
    val clientId: String,
    val ttlLimit: Int,
    val admins: List<String>,
) {
    companion object {
        fun fromConfig(config: Config): AuthConfig {
            val secretKey: String by config
            val clientId: String by config
            val ttlLimit: Int by config
            val admins: List<String> by config

            return AuthConfig(secretKey, clientId, ttlLimit, admins)
        }
    }
}
