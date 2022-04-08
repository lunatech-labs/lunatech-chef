package com.lunatech.chef.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.lunatech.chef.api.auth.RoleAuthorization
import com.lunatech.chef.api.config.AuthConfig
import com.lunatech.chef.api.config.FlywayConfig
import com.lunatech.chef.api.persistence.DBEvolution
import com.lunatech.chef.api.persistence.Database
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.AttendancesWithScheduleInfoService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.LocationsService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesWithInfoService
import com.lunatech.chef.api.persistence.services.UsersService
import com.lunatech.chef.api.routes.ChefSession
import com.lunatech.chef.api.routes.attendances
import com.lunatech.chef.api.routes.attendancesWithScheduleInfo
import com.lunatech.chef.api.routes.authorization
import com.lunatech.chef.api.routes.dishes
import com.lunatech.chef.api.routes.healthCheck
import com.lunatech.chef.api.routes.locations
import com.lunatech.chef.api.routes.menus
import com.lunatech.chef.api.routes.menusWithDishesInfo
import com.lunatech.chef.api.routes.schedules
import com.lunatech.chef.api.routes.schedulesWithAttendanceInfo
import com.lunatech.chef.api.routes.schedulesWithMenusInfo
import com.lunatech.chef.api.routes.users
import com.lunatech.chef.api.routes.validateSession
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.session
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.header
import java.io.File
import java.util.Collections
import mu.KotlinLogging

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)
private val logger = KotlinLogging.logger {}

@Suppress("unused") // Referenced in application.conf
@ExperimentalStdlibApi
fun Application.module() {
    val config = ConfigFactory.load()
    val dbConfig = FlywayConfig.fromConfig(config.getConfig("database"))
    val authConfig = AuthConfig.fromConfig(config.getConfig("auth"))

    val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance())
        .setAudience(Collections.singletonList(authConfig.clientId))
        .build()

    runDBEvolutions(dbConfig)

    val dbConnection = Database.connect(dbConfig)
    val locationsService = LocationsService(dbConnection)
    val dishesService = DishesService(dbConnection)
    val menusService = MenusService(dbConnection)
    val menusWithDishesService = MenusWithDishesNamesService(dbConnection)
    val schedulesService = SchedulesService(dbConnection)
    val schedulesWithInfoService = SchedulesWithInfoService(dbConnection, menusWithDishesService, menusService)
    val usersService = UsersService(dbConnection)
    val attendancesService = AttendancesService(dbConnection, usersService)
    val attendancesWithInfoService = AttendancesWithScheduleInfoService(dbConnection, schedulesService, menusWithDishesService)

    val CHEF_SESSSION = "CHEF_SESSION"
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

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            registerModule(JavaTimeModule()) // support java.time.* types
            registerModule(KotlinModule())
        }
    }

    install(StatusPages) {
        exception<Throwable> { e ->
            call.respondText(e.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.BadRequest)
        }
    }

    // This will add Date and Server headers to each HTTP response besides CHEF_SESSSION header
    install(DefaultHeaders) {
        header(HttpHeaders.AccessControlExposeHeaders, CHEF_SESSSION)
    }

    install(Sessions) {
        header<ChefSession>(CHEF_SESSSION) {
            val secretSignKey = authConfig.secretKey.encodeToByteArray()
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }

    install(Authentication) {
        session<ChefSession>("session-auth") {
            validate { chefSession ->
                validateSession(chefSession, authConfig.ttlLimit)
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }

    install(RoleAuthorization) {
        validate { allowedRoles ->
            // preciso do ChefSession e do allowedRoles
            logger.info("*********** allowedRoles: {}", allowedRoles)
            true
        }
    }

    logger.info { "Booting up!!" }
    routing {
        // Route by default
        get("/") {
            call.respondFile(File("frontend/build/index.html"))
        }
        authorization(usersService, verifier!!, authConfig.admins)
        healthCheck()
        locations(locationsService)
        dishes(dishesService)
        menus(menusService)
        menusWithDishesInfo(menusWithDishesService)
        schedules(schedulesService, attendancesService)
        schedulesWithMenusInfo(schedulesWithInfoService)
        schedulesWithAttendanceInfo(schedulesWithInfoService)
        attendancesWithScheduleInfo(attendancesWithInfoService)
        users(usersService)
        attendances(attendancesService)

        static("static") {
            files("frontend/build/static")
        }
        static("root") {
            files("frontend/build")
        }

        // TODO places to add filter:
        // - Schedules
        // - Who is joining?
        // - Meals scheduled

        // TODO filters to add
        // - date
        // - location

        // TODO Add recurrent schedules
        // TODO FE user profile updates is now showing possible errors
        // TODO proper error when calling endpoint that does not exist
        // TODO HTTPS
        // TODO fix RoleAuthorization
        // TODO swagger and clean routes that are not needed
        // TODO integration with people API ?
        // TODO integration with vacation app ?
        // TODO Automatically add people to a schedule
        // TODO reports -> asked Shelley how they should look like, but she couldnt say
        // TODO replace google sign-in by google identity, check for a new react-google-login
    }
}

private fun runDBEvolutions(flywayConfig: FlywayConfig) = DBEvolution.runDBMigration(flywayConfig)
