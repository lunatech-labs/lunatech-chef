package com.lunatech.chef.api.persistence

import com.lunatech.chef.api.config.FlywayConfig
import org.ktorm.database.Database

object Database {
    fun connect(config: FlywayConfig): Database {
        return Database.connect(url = config.url, user = config.user, password = config.password)
    }
}
