package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.services.AttendancesForSlackbotService
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class AttendancesForSlackbotRoutesTest {
    private lateinit var attendancesForSlackbotService: AttendancesForSlackbotService
    private lateinit var attendancesService: AttendancesService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var usersService: UsersService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testUserUuid: UUID
    private lateinit var testScheduleUuid: UUID
    private lateinit var testUserEmail: String

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        attendancesForSlackbotService = AttendancesForSlackbotService(database)
        officesService = OfficesService(database)
        dishesService = DishesService(database)
        menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService, schedulesService)

        val testOffice = anOffice(city = "Rotterdam")
        officesService.insert(testOffice)
        testOfficeUuid = testOffice.uuid

        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)

        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid

        testUserEmail = uniqueEmail("john")
        val testUser = aUser(name = "John Doe", emailAddress = testUserEmail, officeUuid = testOfficeUuid)
        usersService.insert(testUser)
        testUserUuid = testUser.uuid

        val testSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(3), officeUuid = testOfficeUuid)
        schedulesService.insert(testSchedule)
        testScheduleUuid = testSchedule.uuid
    }

    private fun ApplicationTestBuilder.setupAttendancesForSlackbotRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { attendancesForSlackbot(attendancesForSlackbotService) }
    }

    @Nested
    inner class ParameterValidation {
        @Test
        fun `returns BadRequest without date params`() =
            testApplication {
                setupAttendancesForSlackbotRoutes()

                val response = client.get("/attendancesforslackbot")

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }

        @Test
        fun `returns BadRequest without untildate param`() =
            testApplication {
                setupAttendancesForSlackbotRoutes()

                val response = client.get("/attendancesforslackbot?fromdate=${LocalDate.now()}")

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }

        @Test
        fun `returns BadRequest when untildate before fromdate`() =
            testApplication {
                setupAttendancesForSlackbotRoutes()
                val fromDate = LocalDate.now().plusDays(5)
                val untilDate = LocalDate.now()

                val response = client.get("/attendancesforslackbot?fromdate=$fromDate&untildate=$untilDate")

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }

        @Test
        fun `accepts same fromdate and untildate`() =
            testApplication {
                setupAttendancesForSlackbotRoutes()
                val date = LocalDate.now().plusDays(3)

                val response = client.get("/attendancesforslackbot?fromdate=$date&untildate=$date")

                assertEquals(HttpStatusCode.OK, response.status)
            }
    }

    @Nested
    inner class MissingAttendancesRetrieval {
        @Test
        fun `returns missing attendances when user has null isAttending`() =
            testApplication {
                setupAttendancesForSlackbotRoutes()
                val missingAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = null)
                attendancesService.insert(missingAttendance)

                val fromDate = LocalDate.now()
                val untilDate = LocalDate.now().plusDays(7)

                val response = client.get("/attendancesforslackbot?fromdate=$fromDate&untildate=$untilDate")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains(testUserEmail))
            }

        @Test
        fun `returns empty list when no missing attendances`() =
            testApplication {
                setupAttendancesForSlackbotRoutes()
                val answeredAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
                attendancesService.insert(answeredAttendance)

                val fromDate = LocalDate.now()
                val untilDate = LocalDate.now().plusDays(7)

                val response = client.get("/attendancesforslackbot?fromdate=$fromDate&untildate=$untilDate")

                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[]", response.bodyAsText())
            }

        @Test
        fun `returns empty list when user declined attendance`() =
            testApplication {
                setupAttendancesForSlackbotRoutes()
                val declinedAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = false)
                attendancesService.insert(declinedAttendance)

                val fromDate = LocalDate.now()
                val untilDate = LocalDate.now().plusDays(7)

                val response = client.get("/attendancesforslackbot?fromdate=$fromDate&untildate=$untilDate")

                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[]", response.bodyAsText())
            }
    }
}
