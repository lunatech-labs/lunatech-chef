package com.lunatech.chef.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.lunatech.chef.api.config.FlywayConfig
import com.lunatech.chef.api.config.OauthConfig
import com.lunatech.chef.api.persistence.DBEvolution
import com.lunatech.chef.api.persistence.Database
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.LocationsService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import com.lunatech.chef.api.routes.ChefSession
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
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
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
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val config = ConfigFactory.load()
    val dbConfig = FlywayConfig.fromConfig(config.getConfig("database"))
    val oauthConfig = OauthConfig.fromConfig(config.getConfig("oauth"))

    val googleOauthProvider = OAuthServerSettings.OAuth2ServerSettings(
        name = oauthConfig.name,
        authorizeUrl = oauthConfig.authorizeUrl,
        accessTokenUrl = oauthConfig.accessTokenUrl,
        requestMethod = HttpMethod.Post,
        clientId = oauthConfig.clientId,
        clientSecret = oauthConfig.clientSecret,
        defaultScopes = oauthConfig.defaultScopes
    )

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
        host("localhost:3000")
    }

    install(Sessions) {
        cookie<ChefSession>("LOGGED_USER")
    }
    install(Authentication) {
        oauth("google-oauth") {
            client = HttpClient(Apache)
            providerLookup = { googleOauthProvider }
            urlProvider = { redirectUrl("/login") }
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
            call.respondText(e.localizedMessage, ContentType.Text.Plain, HttpStatusCode.InternalServerError)
        }
    }

    routing {
        // Route by default
        get("/") {
            val session = call.sessions.get<ChefSession>()
            call.respondText("HI ${session?.userId}")
            // call.respondFile(File("frontend/build/index.html"))
        }
        authorization()
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

private fun ApplicationCall.redirectUrl(path: String): String {
    val defaultPort = if (request.origin.scheme == "http") 80 else 443
    val hostPort = request.host()!! + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = request.origin.scheme
    return "$protocol://$hostPort$path"
}
