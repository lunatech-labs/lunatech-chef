package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aRecurrentSchedule
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
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

class RecurrentSchedulesRoutesTest {
    private lateinit var recurrentSchedulesService: RecurrentSchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testMenuUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        recurrentSchedulesService = RecurrentSchedulesService(database)
        menusService = MenusService(database)
        officesService = OfficesService(database)
        dishesService = DishesService(database)

        val testOffice = anOffice(city = "Rotterdam")
        officesService.insert(testOffice)
        testOfficeUuid = testOffice.uuid

        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)

        val testMenu = aMenu(name = "Weekly Lunch", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid
    }

    private fun ApplicationTestBuilder.setupRecurrentSchedulesRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { recurrentSchedules(recurrentSchedulesService) }
    }

    @Nested
    inner class GetAllRecurrentSchedules {
        @Test
        fun `returns empty list when no schedules exist`() =
            testApplication {
                setupRecurrentSchedulesRoutes()

                val response = client.get("/recurrentschedules")

                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[]", response.bodyAsText())
            }

        @Test
        fun `returns all non-deleted recurrent schedules`() =
            testApplication {
                setupRecurrentSchedulesRoutes()
                val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
                recurrentSchedulesService.insert(schedule)

                val response = client.get("/recurrentschedules")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains(testMenuUuid.toString()))
            }
    }

    @Nested
    inner class GetRecurrentScheduleByUuid {
        @Test
        fun `returns recurrent schedule when it exists`() =
            testApplication {
                setupRecurrentSchedulesRoutes()
                val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
                recurrentSchedulesService.insert(schedule)

                val response = client.get("/recurrentschedules/${schedule.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains(schedule.uuid.toString()))
            }

        @Test
        fun `returns NotFound for non-existent UUID`() =
            testApplication {
                setupRecurrentSchedulesRoutes()

                val response = client.get("/recurrentschedules/${UUID.randomUUID()}")

                assertEquals(HttpStatusCode.NotFound, response.status)
            }
    }

    @Nested
    inner class CreateRecurrentSchedule {
        @Test
        fun `creates new recurrent schedule`() =
            testApplication {
                setupRecurrentSchedulesRoutes()
                val client = jsonClient()
                val nextDate = LocalDate.now().plusDays(7)

                val response =
                    client.post("/recurrentschedules") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "menuUuid" to testMenuUuid.toString(),
                                "officeUuid" to testOfficeUuid.toString(),
                                "repetitionDays" to 7,
                                "nextDate" to nextDate.toString(),
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
                val schedules = recurrentSchedulesService.getAll()
                assertEquals(1, schedules.size)
            }

        @Test
        fun `creates recurrent schedule with biweekly repetition`() =
            testApplication {
                setupRecurrentSchedulesRoutes()
                val client = jsonClient()
                val nextDate = LocalDate.now().plusDays(14)

                val response =
                    client.post("/recurrentschedules") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "menuUuid" to testMenuUuid.toString(),
                                "officeUuid" to testOfficeUuid.toString(),
                                "repetitionDays" to 14,
                                "nextDate" to nextDate.toString(),
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
            }

        @Test
        fun `returns BadRequest for invalid JSON`() =
            testApplication {
                setupRecurrentSchedulesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/recurrentschedules") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody("{ invalid json }")
                    }

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }
    }

    @Nested
    inner class UpdateRecurrentSchedule {
        @Test
        fun `updates existing recurrent schedule`() =
            testApplication {
                setupRecurrentSchedulesRoutes()
                val client = jsonClient()
                val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
                recurrentSchedulesService.insert(schedule)
                val newDate = LocalDate.now().plusDays(14)

                val response =
                    client.put("/recurrentschedules/${schedule.uuid}") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "menuUuid" to testMenuUuid.toString(),
                                "officeUuid" to testOfficeUuid.toString(),
                                "repetitionDays" to 14,
                                "nextDate" to newDate.toString(),
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                val updated = recurrentSchedulesService.getByUuid(schedule.uuid)
                assertEquals(14, updated[0].repetitionDays)
            }
    }

    @Nested
    inner class DeleteRecurrentSchedule {
        @Test
        fun `soft deletes recurrent schedule`() =
            testApplication {
                setupRecurrentSchedulesRoutes()
                val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
                recurrentSchedulesService.insert(schedule)

                val response = client.delete("/recurrentschedules/${schedule.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
                val schedules = recurrentSchedulesService.getAll()
                assertTrue(schedules.isEmpty(), "Deleted schedule should not appear in getAll")
            }
    }
}
