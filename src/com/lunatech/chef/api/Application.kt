package com.lunatech.chef.api

import com.lunatech.chef.api.persistence.DBEvolution
import com.lunatech.chef.api.persistence.Database
import com.lunatech.chef.api.persistence.FlywayConfig
import com.lunatech.chef.api.persistence.schemas.Locations
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.select

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

    database.from(Locations).select().map { Locations.createEntity(it) }.map { println() }

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
    //
    // install(Authentication) {
    // }
    //
    // install(ContentNegotiation) {
    //     jackson {
    //         enable(SerializationFeature.INDENT_OUTPUT)
    //     }
    // }
    //
    // val client = HttpClient(Apache) {
    //     install(Logging) {
    //         level = LogLevel.HEADERS
    //     }
    // }
    //
    // routing {
    //     get("/") {
    //         call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
    //     }
    //
    //     install(StatusPages) {
    //         exception<AuthenticationException> { cause ->
    //             call.respond(HttpStatusCode.Unauthorized)
    //         }
    //         exception<AuthorizationException> { cause ->
    //             call.respond(HttpStatusCode.Forbidden)
    //         }
    //     }
    //
    //     get("/json/jackson") {
    //         call.respond(mapOf("hello" to "world"))
    //     }
    // }
}

// class AuthenticationException : RuntimeException()
// class AuthorizationException : RuntimeException()
