package com.lunatech.chef.api.persistence

import com.lunatech.chef.api.config.FlywayConfig
import org.flywaydb.core.Flyway

object DBEvolution {
    fun runDBMigration(config: FlywayConfig) {
        val flyway = Flyway.configure().dataSource(config.url, config.user, config.password).load()
        flyway.migrate()
    }
}
