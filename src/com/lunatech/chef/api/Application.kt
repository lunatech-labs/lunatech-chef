package com.lunatech.chef.api

import com.lunatech.chef.api.persistence.DBEvolution
import io.ktor.application.Application

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    DBEvolution.runDBMigration()

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
