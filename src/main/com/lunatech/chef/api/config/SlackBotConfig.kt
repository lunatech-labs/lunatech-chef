package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class SlackBotConfig(
    val enabled: Boolean,
    val token: String,
    val signingSecret: String,
    val cron: String,
    val publicUrl: String,
) {
    companion object {
        fun fromConfig(config: Config): SlackBotConfig {
            val enabled: Boolean by config
            val token: String by config
            val signingSecret: String by config
            val cron: String by config
            val publicUrl: String by config

            return SlackBotConfig(enabled, token, signingSecret, cron, publicUrl)
        }
    }
}
