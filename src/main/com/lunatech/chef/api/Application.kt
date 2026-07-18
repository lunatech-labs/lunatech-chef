package com.lunatech.chef.api

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lunatech.chef.api.auth.KEYCLOAK_AUTH
import com.lunatech.chef.api.auth.adminOnly
import com.lunatech.chef.api.auth.adminOnlyWrites
import com.lunatech.chef.api.auth.keycloakJwt
import com.lunatech.chef.api.config.FlywayConfig
import com.lunatech.chef.api.config.JwtConfig
import com.lunatech.chef.api.config.MailerConfig
import com.lunatech.chef.api.config.MonthlyReportConfig
import com.lunatech.chef.api.config.SchedulerConfig
import com.lunatech.chef.api.config.SlackBotConfig
import com.lunatech.chef.api.persistence.DBEvolution
import com.lunatech.chef.api.persistence.Database
import com.lunatech.chef.api.persistence.services.AttendancesForSlackbotService
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.AttendancesWithScheduleInfoService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.ExcelService
import com.lunatech.chef.api.persistence.services.ExternalAttendancesService
import com.lunatech.chef.api.persistence.services.ExternalAttendancesWithScheduleInfoService
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
import com.lunatech.chef.api.routes.attendances
import com.lunatech.chef.api.routes.attendancesWithScheduleInfo
import com.lunatech.chef.api.routes.dishes
import com.lunatech.chef.api.routes.externalAttendances
import com.lunatech.chef.api.routes.externalAttendancesWithScheduleInfo
import com.lunatech.chef.api.routes.healthCheck
import com.lunatech.chef.api.routes.me
import com.lunatech.chef.api.routes.menus
import com.lunatech.chef.api.routes.menusWithDishesInfo
import com.lunatech.chef.api.routes.offices
import com.lunatech.chef.api.routes.recurrentSchedules
import com.lunatech.chef.api.routes.recurrentSchedulesWithMenusInfo
import com.lunatech.chef.api.routes.reports
import com.lunatech.chef.api.routes.schedules
import com.lunatech.chef.api.routes.schedulesWithAttendanceInfo
import com.lunatech.chef.api.routes.schedulesWithMenusInfo
import com.lunatech.chef.api.routes.slackInteraction
import com.lunatech.chef.api.routes.users
import com.lunatech.chef.api.schedulers.monthlyreports.mrSchedulerTrigger
import com.lunatech.chef.api.schedulers.recurrentschedules.rcSchedulerTrigger
import com.lunatech.chef.api.schedulers.slackbot.sbSchedulerTrigger
import com.lunatech.chef.api.slackbot.LunchReminderService
import com.lunatech.chef.api.slackbot.SlackApiClient
import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
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
import mu.KotlinLogging
import org.quartz.impl.StdSchedulerFactory
import java.io.File
import java.net.URI
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain
        .main(args)

private val logger = KotlinLogging.logger {}

@Suppress("unused") // Referenced in application.conf
@ExperimentalStdlibApi
fun Application.module() {
    KotlinModule.Builder()

    val config = ConfigFactory.load()
    val dbConfig = FlywayConfig.fromConfig(config.getConfig("database"))
    val jwtConfig = JwtConfig.fromConfig(config.getConfig("jwt"))

    val monthlyReportConfig = MonthlyReportConfig.fromConfig(config.getConfig("monthly-report-email"))
    val mailerConfig = MailerConfig.fromConfig(config.getConfig("mailer"))
    val slackBotConfig = SlackBotConfig.fromConfig(config.getConfig("slackbot"))

    // Cached and rate limited: token signatures are verified on every request.
    val keycloakJwkProvider =
        JwkProviderBuilder(URI(jwtConfig.jwkProvider).toURL())
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

    val recurrentSchedulesConfig = SchedulerConfig.fromConfig(config.getConfig("recurrent-schedules"))
    val monthlyReportsConfig = SchedulerConfig.fromConfig(config.getConfig("monthly-reports"))

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
    val attendancesService = AttendancesService(dbConnection, usersService)
    val externalAttendancesService = ExternalAttendancesService(dbConnection)
    val externalAttendancesWithScheduleInfoService =
        ExternalAttendancesWithScheduleInfoService(dbConnection, schedulesService, menusWithDishesService)
    val attendancesWithInfoService =
        AttendancesWithScheduleInfoService(dbConnection, schedulesService, menusWithDishesService)
    val attendancesForSlackbotService = AttendancesForSlackbotService(dbConnection)
    val reportService = ReportService(dbConnection)
    val excelService = ExcelService()
    val slackHttpClient = HttpClient(Apache)
    val slackApi = SlackApiClient(slackBotConfig.token, slackHttpClient)
    val lunchReminderService = LunchReminderService(attendancesForSlackbotService, slackApi)

    val scheduler = StdSchedulerFactory.getDefaultScheduler()
    if (recurrentSchedulesConfig.enabled) {
        rcSchedulerTrigger(
            scheduler,
            schedulesService,
            recurrentSchedulesService,
            attendancesService,
            externalAttendancesService,
            recurrentSchedulesConfig.cron,
        )
    }
    if (monthlyReportsConfig.enabled) {
        mrSchedulerTrigger(
            scheduler,
            monthlyReportsConfig.cron,
            monthlyReportConfig,
            mailerConfig,
            reportService,
            excelService,
        )
    }
    if (slackBotConfig.enabled) {
        sbSchedulerTrigger(scheduler, lunchReminderService, slackBotConfig.cron)
    }

    install(CORS) {
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.AccessControlAllowHeaders)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.Authorization)
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

    install(DefaultHeaders)

    install(Authentication) {
        keycloakJwt(usersService) {
            verifier(keycloakJwkProvider, jwtConfig.issuer) {
                withAudience(jwtConfig.clientId)
            }
        }
    }

    monitor.subscribe(ApplicationStarted) {
        logger.info("The chef app is ready to roll")
        scheduler.start()
    }
    monitor.subscribe(ApplicationStopped) {
        logger.info("Time to clean up")
        slackHttpClient.close()
    }

    logger.info { "Booting up!!" }
    routing {
        // Default route
        get("/") {
            call.respondFile(File("frontend/build/index.html"))
        }
        healthCheck()
        slackInteraction(attendancesService, slackBotConfig.publicUrl, slackBotConfig.signingSecret)

        authenticate(KEYCLOAK_AUTH) {
            me(usersService)
            menusWithDishesInfo(menusWithDishesService)
            schedulesWithMenusInfo(schedulesWithMenuInfoService)
            schedulesWithAttendanceInfo(schedulesWithAttendanceInfoService)
            recurrentSchedulesWithMenusInfo(recurrentSchedulesMenuWithInfoService)
            attendancesWithScheduleInfo(attendancesWithInfoService)
            users(usersService)
            attendances(attendancesService)
            externalAttendances(externalAttendancesService)
            externalAttendancesWithScheduleInfo(externalAttendancesWithScheduleInfoService)

            adminOnlyWrites {
                offices(officesService)
                dishes(dishesService)
                menus(menusService)
                schedules(schedulesService, attendancesService, externalAttendancesService)
                recurrentSchedules(recurrentSchedulesService)
            }
            adminOnly {
                reports(reportService, excelService)
            }
        }

        singlePageApplication {
            react("frontend/build")
        }
    }
}

private fun runDBEvolutions(flywayConfig: FlywayConfig) = DBEvolution.runDBMigration(flywayConfig)
