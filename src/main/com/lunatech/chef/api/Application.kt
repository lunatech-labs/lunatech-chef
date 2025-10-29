package com.lunatech.chef.api

import com.auth0.jwk.UrlJwkProvider
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lunatech.chef.api.config.AuthConfig
import com.lunatech.chef.api.config.FlywayConfig
import com.lunatech.chef.api.config.JwtConfig
import com.lunatech.chef.api.config.MailerConfig
import com.lunatech.chef.api.config.MonthlyReportConfig
import com.lunatech.chef.api.persistence.DBEvolution
import com.lunatech.chef.api.persistence.Database
import com.lunatech.chef.api.persistence.services.AttendancesForSlackbotService
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.AttendancesWithScheduleInfoService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.ExcelService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesWithMenuInfoService
import com.lunatech.chef.api.persistence.services.ReportService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesWithAttendanceInfoService
import com.lunatech.chef.api.persistence.services.SchedulesWithMenuInfoService
import com.lunatech.chef.api.persistence.services.UsersService
import com.lunatech.chef.api.routes.ChefSession
import com.lunatech.chef.api.routes.attendances
import com.lunatech.chef.api.routes.attendancesForSlackbot
import com.lunatech.chef.api.routes.attendancesWithScheduleInfo
import com.lunatech.chef.api.routes.authentication
import com.lunatech.chef.api.routes.dishes
import com.lunatech.chef.api.routes.healthCheck
import com.lunatech.chef.api.routes.menus
import com.lunatech.chef.api.routes.menusWithDishesInfo
import com.lunatech.chef.api.routes.offices
import com.lunatech.chef.api.routes.recurrentSchedules
import com.lunatech.chef.api.routes.recurrentSchedulesWithMenusInfo
import com.lunatech.chef.api.routes.reports
import com.lunatech.chef.api.routes.schedules
import com.lunatech.chef.api.routes.schedulesWithAttendanceInfo
import com.lunatech.chef.api.routes.schedulesWithMenusInfo
import com.lunatech.chef.api.routes.users
import com.lunatech.chef.api.routes.validateSession
import com.lunatech.chef.api.schedulers.monthlyreports.mrSchedulerTrigger
import com.lunatech.chef.api.schedulers.recurrentschedules.rcSchedulerTrigger
import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.session
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
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
import java.net.URI
import java.time.format.DateTimeParseException
import java.util.Date

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

    val monthlyReportConfig = MonthlyReportConfig.fromConfig(config.getConfig("monthly-report-email"))
    val mailerConfig = MailerConfig.fromConfig(config.getConfig("mailer"))

    val keycloakProvider = UrlJwkProvider(URI(jwtConfig.jwkProvider).toURL())

    val scCronString = config.getString("recurrent-schedules-cron")
    val mrCronString = config.getString("monthly-reports-cron")

    val chefSession = "CHEF_SESSION"

    runDBEvolutions(dbConfig)

    val dbConnection = Database.connect(dbConfig)
    val officesService = OfficesService(dbConnection)
    val dishesService = DishesService(dbConnection)
    val menusService = MenusService(dbConnection)
    val menusWithDishesService = MenusWithDishesNamesService(dbConnection)
    val schedulesService = SchedulesService(dbConnection)
    val recurrentSchedulesService = RecurrentSchedulesService(dbConnection)
    val schedulesWithMenuInfoService = SchedulesWithMenuInfoService(dbConnection, menusWithDishesService)
    val recurrentSchedulesMenuWithInfoService =
        RecurrentSchedulesWithMenuInfoService(dbConnection, menusWithDishesService)
    val schedulesWithAttendanceInfoService = SchedulesWithAttendanceInfoService(dbConnection, menusService)
    val usersService = UsersService(dbConnection)
    val attendancesService = AttendancesService(dbConnection, usersService, schedulesService)
    val attendancesWithInfoService =
        AttendancesWithScheduleInfoService(dbConnection, schedulesService, menusWithDishesService)
    val attendancesForSlackbotService = AttendancesForSlackbotService(dbConnection)
    val reportService = ReportService(dbConnection)
    val excelService = ExcelService()

    val scheduler = StdSchedulerFactory.getDefaultScheduler()
    rcSchedulerTrigger(scheduler, schedulesService, recurrentSchedulesService, attendancesService, scCronString)
    mrSchedulerTrigger(scheduler, mrCronString, monthlyReportConfig, mailerConfig, reportService, excelService)

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

    // handles exceptions
    install(StatusPages) {
        exception<Throwable> { call, throwable ->
            when (throwable) {
                is IllegalArgumentException, is DateTimeParseException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "${throwable.message}",
                    )
                }
            }
        }
    }

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            registerModule(JavaTimeModule()) // support java.time.* types
        }
    }

    // This will add Date and Server headers to each HTTP response besides CHEF_SESSION header
    // it's needed when start BE and FE separately
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
                call.respond(HttpStatusCode.Unauthorized, "Session is not valid or has expired")
            }
        }

        /***
         * This validates tokens used by the slack bot to communicate with lunachef
         */
        jwt("auth-jwt") {
            verifier(keycloakProvider, jwtConfig.issuer)
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
            validate { credential ->
                if (credential.expiresAt?.after(Date()) == true &&
                    credential.payload.getClaim("azp")
                        .asString() != ""
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }

        /***
         * This validates the Google idToken obtained at login
         */
        jwt("idtoken") {
            verifier(keycloakProvider, jwtConfig.issuer)
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "IdToken is not valid or has expired")
            }
            validate { credential ->
                if (credential.expiresAt?.after(Date()) == true &&
                    credential.payload.getClaim("email_verified").asBoolean()
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    monitor.subscribe(ApplicationStarted) {
        logger.info("The chef app is ready to roll")
        scheduler.start()
    }
    monitor.subscribe(ApplicationStopped) {
        logger.info("Time to clean up")
    }

    logger.info { "Booting up!!" }
    routing {
        // Default route
        get("/") {
            call.respondFile(File("frontend/build/index.html"))
        }
        healthCheck()
        authentication(schedulesService, attendancesService, usersService, authConfig.admins)

        authenticate("session-auth", "auth-jwt") {
            offices(officesService)
            dishes(dishesService)
            menus(menusService)
            menusWithDishesInfo(menusWithDishesService)
            schedules(schedulesService, attendancesService)
            schedulesWithMenusInfo(schedulesWithMenuInfoService)
            schedulesWithAttendanceInfo(schedulesWithAttendanceInfoService)
            recurrentSchedules(recurrentSchedulesService)
            recurrentSchedulesWithMenusInfo(recurrentSchedulesMenuWithInfoService)
            attendancesWithScheduleInfo(attendancesWithInfoService)
            users(usersService)
            attendances(attendancesService)
            attendancesForSlackbot(attendancesForSlackbotService)
            reports(reportService, excelService)
        }

        singlePageApplication {
            react("frontend/build")
        }
    }
}

private fun runDBEvolutions(flywayConfig: FlywayConfig) = DBEvolution.runDBMigration(flywayConfig)
