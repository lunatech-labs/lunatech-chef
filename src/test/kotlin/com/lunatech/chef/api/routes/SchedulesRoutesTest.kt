package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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

class SchedulesRoutesTest {
    private lateinit var schedulesService: SchedulesService
    private lateinit var attendancesService: AttendancesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var usersService: UsersService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testMenuUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
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

        // Create a test user for attendance creation
        val testUser = aUser(name = "Test User", emailAddress = uniqueEmail("test"), officeUuid = testOfficeUuid)
        usersService.insert(testUser)
    }

    private fun ApplicationTestBuilder.setupSchedulesRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { schedules(schedulesService, attendancesService) }
    }

    @Nested
    inner class GetAllSchedules {
        @Test
        fun `returns empty list when no schedules exist`() =
            testApplication {
                setupSchedulesRoutes()

                val response = client.get("/schedules")

                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[]", response.bodyAsText())
            }

        @Test
        fun `returns all non-deleted schedules`() =
            testApplication {
                setupSchedulesRoutes()
                val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
                schedulesService.insert(schedule)

                val response = client.get("/schedules")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains(testMenuUuid.toString()))
            }
    }

    @Nested
    inner class GetScheduleByUuid {
        @Test
        fun `returns schedule when it exists`() =
            testApplication {
                setupSchedulesRoutes()
                val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
                schedulesService.insert(schedule)

                val response = client.get("/schedules/${schedule.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains(schedule.uuid.toString()))
            }

        @Test
        fun `returns NotFound for non-existent UUID`() =
            testApplication {
                setupSchedulesRoutes()

                val response = client.get("/schedules/${UUID.randomUUID()}")

                assertEquals(HttpStatusCode.NotFound, response.status)
            }
    }

    @Nested
    inner class CreateSchedule {
        @Test
        fun `creates new schedule with attendances`() =
            testApplication {
                setupSchedulesRoutes()
                val client = jsonClient()
                val scheduleDate = LocalDate.now().plusDays(14)

                val response =
                    client.post("/schedules") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "menuUuid" to testMenuUuid.toString(),
                                "date" to scheduleDate.toString(),
                                "officeUuid" to testOfficeUuid.toString(),
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
                val schedules = schedulesService.getAll()
                assertEquals(1, schedules.size)
            }

        @Test
        fun `returns BadRequest for invalid JSON`() =
            testApplication {
                setupSchedulesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/schedules") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody("{ invalid json }")
                    }

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }
    }

    @Nested
    inner class UpdateSchedule {
        @Test
        fun `updates existing schedule`() =
            testApplication {
                setupSchedulesRoutes()
                val client = jsonClient()
                val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
                schedulesService.insert(schedule)
                val newDate = LocalDate.now().plusDays(14)

                val response =
                    client.put("/schedules/${schedule.uuid}") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "menuUuid" to testMenuUuid.toString(),
                                "date" to newDate.toString(),
                                "officeUuid" to testOfficeUuid.toString(),
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                val updated = schedulesService.getByUuid(schedule.uuid)
                assertEquals(newDate, updated[0].date)
            }
    }

    @Nested
    inner class DeleteSchedule {
        @Test
        fun `soft deletes schedule`() =
            testApplication {
                setupSchedulesRoutes()
                val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
                schedulesService.insert(schedule)

                val response = client.delete("/schedules/${schedule.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
                val schedules = schedulesService.getAll()
                assertTrue(schedules.isEmpty(), "Deleted schedule should not appear in getAll")
            }
    }
}
