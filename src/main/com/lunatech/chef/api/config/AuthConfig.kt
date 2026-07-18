package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class AuthConfig(
    val secretKey: String,
    val ttlLimit: Int,
) {
    companion object {
        fun fromConfig(config: Config): AuthConfig {
            val secretKey: String by config
            val ttlLimit: Int by config

            return AuthConfig(secretKey, ttlLimit)
        }
    }
}
