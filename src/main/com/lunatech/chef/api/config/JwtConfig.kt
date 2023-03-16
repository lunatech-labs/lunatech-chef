package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class JwtConfig(
    val secretKey: String,
    val issuer: String,
    val ttlLimit: Int,
) {
    companion object {
        fun fromConfig(config: Config): JwtConfig {
            val secretKey: String by config
            val ttlLimit: Int by config
            val issuer: String by config

            return JwtConfig(secretKey,issuer,ttlLimit)
        }
    }
}
