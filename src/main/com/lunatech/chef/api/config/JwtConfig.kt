package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class JwtConfig(
    val clientId: String,
    val jwkProvider: String,
    val issuer: String,
) {
    companion object {
        fun fromConfig(config: Config): JwtConfig {
            val clientId: String by config
            val jwkProvider: String by config
            val issuer: String by config

            return JwtConfig(clientId, jwkProvider, issuer)
        }
    }
}
