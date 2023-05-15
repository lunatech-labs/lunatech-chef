package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class MonthlyReportConfig(
    val subject: String,
    val from: String,
    val to: String,
) {
    companion object {
        fun fromConfig(config: Config): MonthlyReportConfig {
            val subject: String by config
            val from: String by config
            val to: String by config

            return MonthlyReportConfig(subject, from, to)
        }
    }
}
