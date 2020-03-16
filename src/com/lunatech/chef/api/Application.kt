package com.lunatech.chef.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.lunatech.chef.api.persistence.DBEvolution
import com.lunatech.chef.api.persistence.Database
import com.lunatech.chef.api.persistence.FlywayConfig
import com.lunatech.chef.api.routes.healthCheck
import com.lunatech.chef.api.routes.locations
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val flywayConfig = FlywayConfig.fromConfig(
        ConfigFactory.load().getConfig("flyway")
    )

    DBEvolution.runDBMigration(flywayConfig)

    // TODO singletons? injection?
    val database = Database.connect(flywayConfig)
    // database.from(Locations).select().map { Locations.createEntity(it) }.map { println() }

    // install(CORS) {
    //     method(HttpMethod.Options)
    //     method(HttpMethod.Put)
    //     method(HttpMethod.Delete)
    //     method(HttpMethod.Patch)
    //     header(HttpHeaders.Authorization)
    //     header("MyCustomHeader")
    //     allowCredentials = true
    //     anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    // }

    // install(Authentication) {
    // }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(StatusPages) {
        exception<Throwable> { e ->
            call.respondText(e.localizedMessage, ContentType.Text.Plain, HttpStatusCode.InternalServerError)
        }
        // exception<AuthenticationException> { cause ->
        //     call.respond(HttpStatusCode.Unauthorized)
        // }
        // exception<AuthorizationException> { cause ->
        //     call.respond(HttpStatusCode.Forbidden)
        // }
    }

    val client = HttpClient(Apache) {
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }

    routing {
        healthCheck()
        locations(database)

        get("/") {
            call.respondText("Hello world!", contentType = ContentType.Text.Plain)
        }
    }
}

// class AuthenticationException : RuntimeException()
// class AuthorizationException : RuntimeException()
