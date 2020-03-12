package com.lunatech.chef.api.persistence

import me.liuwj.ktorm.database.Database

object Database {
    fun connect(config: FlywayConfig): Database {
        return Database.connect(url = config.url, user = config.user, password = config.password)
    }
}
