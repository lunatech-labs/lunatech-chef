package com.lunatech.chef.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.lunatech.chef.api.config.AuthConfig
import com.lunatech.chef.api.config.FlywayConfig
import com.lunatech.chef.api.config.JwtConfig
import com.lunatech.chef.api.persistence.DBEvolution
import com.lunatech.chef.api.persistence.Database
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.AttendancesWithScheduleInfoService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.LocationsService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesWithMenuInfo
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesWithAttendanceInfo
import com.lunatech.chef.api.persistence.services.SchedulesWithMenuInfo
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
import com.lunatech.chef.api.routes.recurrentSchedules
import com.lunatech.chef.api.routes.recurrentSchedulesWithMenusInfo
import com.lunatech.chef.api.routes.schedules
import com.lunatech.chef.api.routes.schedulesWithAttendanceInfo
import com.lunatech.chef.api.routes.schedulesWithMenusInfo
import com.lunatech.chef.api.routes.users
import com.lunatech.chef.api.routes.validateSession
import com.lunatech.chef.api.schedulers.schedulerTrigger
import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.session
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.header
import mu.KotlinLogging
import org.quartz.impl.StdSchedulerFactory
import java.io.File
import java.util.Collections

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)
private val logger = KotlinLogging.logger {}

@Suppress("unused") // Referenced in application.conf
@ExperimentalStdlibApi
fun Application.module() {
    KotlinModule.Builder()

    val config = ConfigFactory.load()
    val dbConfig = FlywayConfig.fromConfig(config.getConfig("database"))
    val authConfig = AuthConfig.fromConfig(config.getConfig("auth"))
    val jwtConfig = JwtConfig.fromConfig(config.getConfig("jwt"))
    val cronString = config.getString("recurrent-schedules-cron")

    val chefSession = "CHEF_SESSION"

    val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(Collections.singletonList(authConfig.clientId))
        .build()

    runDBEvolutions(dbConfig)

    val dbConnection = Database.connect(dbConfig)
    val locationsService = LocationsService(dbConnection)
    val dishesService = DishesService(dbConnection)
    val menusService = MenusService(dbConnection)
    val menusWithDishesService = MenusWithDishesNamesService(dbConnection)
    val schedulesService = SchedulesService(dbConnection)
    val recurrentSchedulesService = RecurrentSchedulesService(dbConnection)
    val schedulesWithMenuInfoService = SchedulesWithMenuInfo(dbConnection, menusWithDishesService)
    val recurrentSchedulesMenuWithInfoService = RecurrentSchedulesWithMenuInfo(dbConnection, menusWithDishesService)
    val schedulesWithAttendanceInfoService = SchedulesWithAttendanceInfo(dbConnection, menusService)
    val usersService = UsersService(dbConnection)
    val attendancesService = AttendancesService(dbConnection, usersService)
    val attendancesWithInfoService =
        AttendancesWithScheduleInfoService(dbConnection, schedulesService, menusWithDishesService)

    val scheduler = StdSchedulerFactory.getDefaultScheduler()
    schedulerTrigger(scheduler, schedulesService, recurrentSchedulesService, attendancesService, cronString)

    install(CORS) {
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.AccessControlAllowHeaders)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(chefSession)
        anyHost()
    }

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            registerModule(JavaTimeModule()) // support java.time.* types
        }
    }

//    install(StatusPages) {
//        exception<Throwable> { e ->
//            call.respondText(e.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.BadRequest)
//        }
//    }

    // This will add Date and Server headers to each HTTP response besides CHEF_SESSION header
    install(DefaultHeaders) {
        header(HttpHeaders.AccessControlExposeHeaders, chefSession)
    }

    install(Sessions) {
        header<ChefSession>(chefSession) {
            val secretSignKey = authConfig.secretKey.encodeToByteArray()
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }

    install(Authentication) {
        session("session-auth") {
            validate { chefSession ->
                validateSession(chefSession, authConfig.ttlLimit)
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secretKey))
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }



//    install(RoleAuthorization) {
//        validate { allowedRoles ->
//            // need ChefSession and allowedRoles
//            logger.info("*********** allowedRoles: {}", allowedRoles)
//            true
//        }
//    }

    environment.monitor.subscribe(ApplicationStarted) {
        logger.info("The chef app is ready to roll")
        scheduler.start()
    }
    environment.monitor.subscribe(ApplicationStopped) {
        logger.info("Time to clean up")
//        scheduler.shutdown() # the shutdown is problematic
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
        schedulesWithMenusInfo(schedulesWithMenuInfoService)
        schedulesWithAttendanceInfo(schedulesWithAttendanceInfoService)
        recurrentSchedules(recurrentSchedulesService)
        recurrentSchedulesWithMenusInfo(recurrentSchedulesMenuWithInfoService)
        attendancesWithScheduleInfo(attendancesWithInfoService)
        users(usersService,jwtConfig)
        attendances(attendancesService)

        singlePageApplication {
            react("frontend/build")
        }

        // TODO add cancel buttons to add/edit panels to improve navigation
        // TODO calling PUT with an uuid that does exist throws error

        // TODO FE user profile updates is not showing possible errors
        // TODO proper error when calling endpoint that does not exist
        // TODO HTTPS
        // TODO fix RoleAuthorization
        // TODO swagger and clean routes that are not needed
        // TODO integration with people API ?
        // TODO integration with vacation app ?
    }
}

private fun runDBEvolutions(flywayConfig: FlywayConfig) = DBEvolution.runDBMigration(flywayConfig)
