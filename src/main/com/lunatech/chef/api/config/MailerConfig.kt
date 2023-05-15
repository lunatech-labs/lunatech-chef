package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class MailerConfig(
    val host: String,
    val user: String,
    val password: String,
    val port: Int,
) {
    companion object {
        fun fromConfig(config: Config): MailerConfig {
            val host: String by config
            val user: String by config
            val password: String by config
            val port: Int by config

            return MailerConfig(host, user, password, port)
        }
    }
}
