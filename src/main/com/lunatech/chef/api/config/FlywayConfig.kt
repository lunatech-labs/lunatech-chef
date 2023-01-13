package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class FlywayConfig(val url: String, val user: String, val password: String, val maxPoolSize: Int) {
    companion object {
        fun fromConfig(config: Config): FlywayConfig {
            val url: String by config
            val user: String by config
            val password: String by config
            val maxPoolSize: Int by config

            return FlywayConfig(
                url,
                user,
                password,
                maxPoolSize
            )
        }
    }
}
