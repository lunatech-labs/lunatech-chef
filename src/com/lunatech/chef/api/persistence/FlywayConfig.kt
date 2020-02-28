package com.lunatech.chef.api.persistence

import com.typesafe.config.Config
import io.github.config4k.getValue

data class FlywayConfig(val url: String, val user: String, val password: String) {
    companion object {
        fun fromConfig(config: Config): FlywayConfig {
            val url: String by config
            val user: String by config
            val password: String by config

            return FlywayConfig(
                url,
                user,
                password)
        }
    }
}
