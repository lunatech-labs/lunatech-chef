package com.lunatech.chef.api.persistence

import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway

object DBEvolution {
    fun runDBMigration() {
        val flywayConfig = FlywayConfig.fromConfig(
            ConfigFactory.load().getConfig("flyway")
        )
        val flyway = Flyway.configure().dataSource(flywayConfig.url, flywayConfig.user, flywayConfig.password).load()
        flyway.migrate()
    }
}
