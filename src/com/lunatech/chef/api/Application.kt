package com.lunatech.chef.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.lunatech.chef.api.config.FlywayConfig
import com.lunatech.chef.api.persistence.DBEvolution
import com.lunatech.chef.api.persistence.Database
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.LocationsService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import com.lunatech.chef.api.routes.attendances
import com.lunatech.chef.api.routes.authorization
import com.lunatech.chef.api.routes.dishes
import com.lunatech.chef.api.routes.healthCheck
import com.lunatech.chef.api.routes.locations
import com.lunatech.chef.api.routes.menus
import com.lunatech.chef.api.routes.menusWithDishesNames
import com.lunatech.chef.api.routes.schedules
import com.lunatech.chef.api.routes.users
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.session
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.features.origin
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.get
import io.ktor.sessions.header
import io.ktor.sessions.sessions
import java.io.File
import java.util.Collections
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ChefSession(val userEmail: String, val name: String, val isAdmin: Boolean)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
@ExperimentalStdlibApi
fun Application.module(testing: Boolean = false) {
    val CHEF_SESSSION = "CHEF_SESSION"
    val config = ConfigFactory.load()
    val dbConfig = FlywayConfig.fromConfig(config.getConfig("database"))
    val secretKey = environment.config.property("app.session.secretKey").getString()
    val clientId = environment.config.property("app.session.clientId").getString()

    val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance())
        .setAudience(Collections.singletonList(clientId))
        .build()

    logger.info("GoogleIdTokenVerifier created: {}", verifier)

    runDBEvolutions(dbConfig)

    val dbConnection = Database.connect(dbConfig)
    val locationsService = LocationsService(dbConnection)
    val dishesService = DishesService(dbConnection)
    val menusService = MenusService(dbConnection)
    val menusWithDishesService = MenusWithDishesNamesService(dbConnection)
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
        header(HttpHeaders.Authorization)
        header(CHEF_SESSSION)
        host("localhost:3000")
    }

    install(DefaultHeaders) {
        header(HttpHeaders.AccessControlExposeHeaders, CHEF_SESSSION)
    }

    install(Sessions) {
        header<ChefSession>(CHEF_SESSSION) {
            val secretSignKey = secretKey.encodeToByteArray()
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }

    install(Authentication) {
        session<ChefSession>("session-auth") {
            data class AccountPrincipal(val email: String) : Principal
            validate { sessionAccount ->
                // validar info da cookie
                AccountPrincipal("leonor.boga@lunatech.com")
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            registerModule(JavaTimeModule()) // support java.time.* types
        }
    }
    install(StatusPages) {
        exception<Throwable> { e ->
            call.respondText(e.message ?: "", ContentType.Text.Plain, HttpStatusCode.InternalServerError)
        }
    }

    routing {
        // Route by default
        get("/") {
            val session = call.sessions.get<ChefSession>()
            call.respondFile(File("frontend/build/index.html"))
        }
        authorization(verifier!!)
        healthCheck()
        locations(locationsService)
        dishes(dishesService)
        menus(menusService)
        menusWithDishesNames(menusWithDishesService)
        schedules(schedulesService)
        users(usersService)
        attendances(attendancesService)

        static("static") {
            files("frontend/build/static")
        }
        static("root") {
            files("frontend/build")
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

private fun runDBEvolutions(flywayConfig: FlywayConfig) = DBEvolution.runDBMigration(flywayConfig)

fun ApplicationCall.redirectUrl(path: String): String {
    val defaultPort = if (request.origin.scheme == "http") 80 else 443
    val hostPort = request.host() + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = request.origin.scheme
    return "$protocol://$hostPort$path"
}
