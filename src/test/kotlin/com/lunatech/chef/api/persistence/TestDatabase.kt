package com.lunatech.chef.api.persistence

import org.flywaydb.core.Flyway
import org.ktorm.database.Database
import org.testcontainers.containers.PostgreSQLContainer

object TestDatabase {
    private var database: Database? = null

    private val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15-alpine")
        .withDatabaseName("lunatech-chef-test")
        .withUsername("test")
        .withPassword("test")

    fun getDatabase(): Database {
        if (database == null) {
            if (!postgresContainer.isRunning) {
                postgresContainer.start()
            }

            val jdbcUrl = postgresContainer.jdbcUrl
            val user = postgresContainer.username
            val password = postgresContainer.password

            // Run Flyway migrations using the existing migration scripts
            Flyway.configure()
                .dataSource(jdbcUrl, user, password)
                .locations("classpath:db/migration")
                .load()
                .migrate()

            database = Database.connect(
                url = jdbcUrl,
                user = user,
                password = password
            )
        }
        return database!!
    }

    fun resetDatabase() {
        database?.useConnection { conn ->
            val tables = listOf(
                "attendances", "users", "schedules", "recurrent_schedules",
                "dishes_on_menus", "menus", "dishes", "offices"
            )
            tables.forEach { table ->
                conn.createStatement().execute("TRUNCATE TABLE $table CASCADE")
            }
        }
    }
}
