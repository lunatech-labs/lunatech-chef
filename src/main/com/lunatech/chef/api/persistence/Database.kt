package com.lunatech.chef.api.persistence

import com.lunatech.chef.api.config.FlywayConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.ktorm.database.Database

object Database {
    fun connect(config: FlywayConfig): Database {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            username = config.user
            password = config.password
            maximumPoolSize = 4
        }
        val dataSource = HikariDataSource(hikariConfig)
        return Database.connect(dataSource)
    }
}
