package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class JwtConfig(
    val jwkProvider: String,
    val issuer: String,
    val clientId: String,
) {
    companion object {
        fun fromConfig(config: Config): JwtConfig {
            val jwkProvider: String by config
            val issuer: String by config
            val clientId: String by config

            return JwtConfig(jwkProvider, issuer, clientId)
        }
    }
}
