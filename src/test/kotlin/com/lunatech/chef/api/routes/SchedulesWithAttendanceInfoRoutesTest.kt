package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesWithAttendanceInfoService
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

class SchedulesWithAttendanceInfoRoutesTest {
    private lateinit var schedulesWithAttendanceInfoService: SchedulesWithAttendanceInfoService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var usersService: UsersService
    private lateinit var attendancesService: AttendancesService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testUserUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        dishesService = DishesService(database)
        officesService = OfficesService(database)
        menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService, schedulesService)
        schedulesWithAttendanceInfoService = SchedulesWithAttendanceInfoService(database, menusService)

        val testOffice = anOffice(city = "Rotterdam")
        officesService.insert(testOffice)
        testOfficeUuid = testOffice.uuid

        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)

        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid

        val testUser = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
        usersService.insert(testUser)
        testUserUuid = testUser.uuid
    }

    private fun ApplicationTestBuilder.setupSchedulesWithAttendanceInfoRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { schedulesWithAttendanceInfo(schedulesWithAttendanceInfoService) }
    }

    @Nested
    inner class GetAllSchedulesWithAttendanceInfo {
        @Test
        fun `returns empty list when no schedules exist`() =
            testApplication {
                setupSchedulesWithAttendanceInfoRoutes()

                val response = client.get("/schedulesWithAttendanceInfo")

                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[]", response.bodyAsText())
            }

        @Test
        fun `returns schedules with attendants`() =
            testApplication {
                setupSchedulesWithAttendanceInfoRoutes()
                val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
                schedulesService.insert(schedule)

                val attendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUserUuid, isAttending = true)
                attendancesService.insert(attendance)

                val response = client.get("/schedulesWithAttendanceInfo")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Lunch Menu"))
                assertTrue(response.bodyAsText().contains("John Doe"))
            }
    }

    @Nested
    inner class FilteringSchedulesWithAttendance {
        @Test
        fun `filters by fromdate`() =
            testApplication {
                setupSchedulesWithAttendanceInfoRoutes()
                val futureSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(5), officeUuid = testOfficeUuid)
                schedulesService.insert(futureSchedule)

                val response = client.get("/schedulesWithAttendanceInfo?fromdate=${LocalDate.now()}")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains(futureSchedule.uuid.toString()))
            }

        @Test
        fun `filters by office`() =
            testApplication {
                setupSchedulesWithAttendanceInfoRoutes()
                val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
                schedulesService.insert(schedule)

                val response = client.get("/schedulesWithAttendanceInfo?office=$testOfficeUuid")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Rotterdam"))
            }
    }
}
