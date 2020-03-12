package com.lunatech.chef.api.persistence

import org.flywaydb.core.Flyway

object DBEvolution {
    fun runDBMigration(config: FlywayConfig) {

        val flyway = Flyway.configure().dataSource(config.url, config.user, config.password).load()
        flyway.migrate()
    }
}
