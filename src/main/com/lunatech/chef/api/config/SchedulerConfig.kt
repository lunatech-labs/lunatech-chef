package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class SchedulerConfig(
    val enabled: Boolean,
    val cron: String,
) {
    companion object {
        fun fromConfig(config: Config): SchedulerConfig {
            val enabled: Boolean by config
            val cron: String by config

            return SchedulerConfig(enabled, cron)
        }
    }
}
