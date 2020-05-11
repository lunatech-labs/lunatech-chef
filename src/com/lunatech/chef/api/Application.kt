package com.lunatech.chef.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.lunatech.chef.api.persistence.DBEvolution
import com.lunatech.chef.api.persistence.Database
import com.lunatech.chef.api.persistence.FlywayConfig
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.LocationsService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import com.lunatech.chef.api.routes.attendances
import com.lunatech.chef.api.routes.dishes
import com.lunatech.chef.api.routes.healthCheck
import com.lunatech.chef.api.routes.locations
import com.lunatech.chef.api.routes.menus
import com.lunatech.chef.api.routes.schedules
import com.lunatech.chef.api.routes.users
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val dbConfig = FlywayConfig.fromConfig(
        ConfigFactory.load().getConfig("database")
    )
    runDBEvolutions(dbConfig)

    val dbConnection = Database.connect(dbConfig)
    val locationsService = LocationsService(dbConnection)
    val dishesService = DishesService(dbConnection)
    val menusService = MenusService(dbConnection)
    val schedulesService = SchedulesService(dbConnection)
    val usersService = UsersService(dbConnection)
    val attendancesService = AttendancesService(dbConnection)

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.AccessControlAllowOrigin)
        // header(HttpHeaders.Authorization)
        // allowCredentials = true
        host("localhost:3000")
    }

    // install(Authentication) {
    // }

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            registerModule(JavaTimeModule()) // support java.time.* types
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

    // val client = HttpClient(Apache) {
    //     install(Logging) {
    //         level = LogLevel.HEADERS
    //     }
    // }

    routing {
        healthCheck()
        locations(locationsService)
        dishes(dishesService)
        menus(menusService)
        schedules(schedulesService)
        users(usersService)
        attendances(attendancesService)

        static("static") {
            files("frontend/build/static")
        }
        static("root") {
            files("frontend/build")
        }
        // Route by default
        get("/{...}") {
            call.respondFile(File("frontend/build/index.html"))
        }

        // TODO authorization, login, logout
        // TODO filtros no attendances, schedules por data, localizacao
        // TODO swagger
        // TODO pagina principal? filtrar por localizacao, lista cronologica
        // TODO reports
        // TODO integration com a people API
        // TODO integration com a vacation app
        // TODO adicionar pessoas a um schedule automaticamente
        // TODO ver notas sobre outras features, como os schedules recorrentes
    }
}

fun runDBEvolutions(flywayConfig: FlywayConfig) = DBEvolution.runDBMigration(flywayConfig)

// class AuthenticationException : RuntimeException()
// class AuthorizationException : RuntimeException()
