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
import com.lunatech.chef.api.persistence.services.ExcelService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.ReportService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
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

class ReportsRoutesTest {
    private lateinit var reportService: ReportService
    private lateinit var excelService: ExcelService
    private lateinit var attendancesService: AttendancesService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var usersService: UsersService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testUserUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        reportService = ReportService(database)
        excelService = ExcelService()
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

        val testUser = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
        usersService.insert(testUser)
        testUserUuid = testUser.uuid
    }

    private fun ApplicationTestBuilder.setupReportsRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { reports(reportService, excelService) }
    }

    @Nested
    inner class ParameterValidation {
        @Test
        fun `returns BadRequest without year and month params`() =
            testApplication {
                setupReportsRoutes()

                val response = client.get("/reports")

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }

        @Test
        fun `returns BadRequest without month param`() =
            testApplication {
                setupReportsRoutes()

                val response = client.get("/reports?year=2024")

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }

        @Test
        fun `returns BadRequest without year param`() =
            testApplication {
                setupReportsRoutes()

                val response = client.get("/reports?month=6")

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }

        @Test
        fun `returns BadRequest for invalid year format`() =
            testApplication {
                setupReportsRoutes()

                val response = client.get("/reports?year=invalid&month=6")

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }

        @Test
        fun `returns BadRequest for invalid month format`() =
            testApplication {
                setupReportsRoutes()

                val response = client.get("/reports?year=2024&month=invalid")

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }
    }

    @Nested
    inner class ReportGeneration {
        @Test
        fun `returns OK with year and month params`() =
            testApplication {
                setupReportsRoutes()
                val today = LocalDate.now()
                val scheduleDate = today.withDayOfMonth(15)
                val schedule = aSchedule(menuUuid = testMenuUuid, date = scheduleDate, officeUuid = testOfficeUuid)
                schedulesService.insert(schedule)

                val attendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUserUuid, isAttending = true)
                attendancesService.insert(attendance)

                val response = client.get("/reports?year=${today.year}&month=${today.monthValue}")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.headers[HttpHeaders.ContentType]?.contains("spreadsheet") == true)
            }

        @Test
        fun `returns OK for month with no data`() =
            testApplication {
                setupReportsRoutes()

                val response = client.get("/reports?year=2020&month=1")

                assertEquals(HttpStatusCode.OK, response.status)
            }

        @Test
        fun `returns spreadsheet content type`() =
            testApplication {
                setupReportsRoutes()
                val today = LocalDate.now()

                val response = client.get("/reports?year=${today.year}&month=${today.monthValue}")

                assertEquals(HttpStatusCode.OK, response.status)
            }
    }
}
