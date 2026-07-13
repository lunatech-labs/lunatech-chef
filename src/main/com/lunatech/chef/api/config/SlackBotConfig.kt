package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class SlackBotConfig(
    val token: String,
    val cron: String,
    val publicUrl: String,
) {
    companion object {
        fun fromConfig(config: Config): SlackBotConfig {
            val token: String by config
            val cron: String by config
            val publicUrl: String by config

            return SlackBotConfig(token, cron, publicUrl)
        }
    }
}
