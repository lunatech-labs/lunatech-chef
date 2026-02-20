package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aRecurrentSchedule
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesWithMenuInfoService
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

class RecurrentSchedulesWithMenusInfoRoutesTest {
    private lateinit var recurrentSchedulesWithMenuInfoService: RecurrentSchedulesWithMenuInfoService
    private lateinit var recurrentSchedulesService: RecurrentSchedulesService
    private lateinit var menusService: MenusService
    private lateinit var menusWithDishesNamesService: MenusWithDishesNamesService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testOffice2Uuid: UUID
    private lateinit var testMenuUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        dishesService = DishesService(database)
        officesService = OfficesService(database)
        menusService = MenusService(database)
        menusWithDishesNamesService = MenusWithDishesNamesService(database)
        recurrentSchedulesService = RecurrentSchedulesService(database)
        recurrentSchedulesWithMenuInfoService = RecurrentSchedulesWithMenuInfoService(database, menusWithDishesNamesService)

        val testOffice = anOffice(city = "Rotterdam")
        val testOffice2 = anOffice(city = "Paris", country = "France")
        officesService.insert(testOffice)
        officesService.insert(testOffice2)
        testOfficeUuid = testOffice.uuid
        testOffice2Uuid = testOffice2.uuid

        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)

        val testMenu = aMenu(name = "Weekly Lunch", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid
    }

    private fun ApplicationTestBuilder.setupRecurrentSchedulesWithMenusInfoRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { recurrentSchedulesWithMenusInfo(recurrentSchedulesWithMenuInfoService) }
    }

    @Nested
    inner class GetAllRecurrentSchedulesWithMenusInfo {
        @Test
        fun `returns empty list when no schedules exist`() =
            testApplication {
                setupRecurrentSchedulesWithMenusInfoRoutes()

                val response = client.get("/recurrentSchedulesWithMenusInfo")

                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[]", response.bodyAsText())
            }

        @Test
        fun `returns schedules with menu and office info`() =
            testApplication {
                setupRecurrentSchedulesWithMenusInfoRoutes()
                val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
                recurrentSchedulesService.insert(schedule)

                val response = client.get("/recurrentSchedulesWithMenusInfo")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Weekly Lunch"))
                assertTrue(response.bodyAsText().contains("Rotterdam"))
            }
    }

    @Nested
    inner class FilteringRecurrentSchedules {
        @Test
        fun `filters by office`() =
            testApplication {
                setupRecurrentSchedulesWithMenusInfoRoutes()
                val schedule1 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
                val schedule2 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOffice2Uuid, repetitionDays = 7)
                recurrentSchedulesService.insert(schedule1)
                recurrentSchedulesService.insert(schedule2)

                val response = client.get("/recurrentSchedulesWithMenusInfo?office=$testOfficeUuid")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Rotterdam"))
            }
    }

    @Nested
    inner class GetRecurrentScheduleWithMenusInfoByUuid {
        @Test
        fun `returns schedule with info`() =
            testApplication {
                setupRecurrentSchedulesWithMenusInfoRoutes()
                val schedule =
                    aRecurrentSchedule(
                        menuUuid = testMenuUuid,
                        officeUuid = testOfficeUuid,
                        repetitionDays = 14,
                        nextDate = LocalDate.now().plusDays(14),
                    )
                recurrentSchedulesService.insert(schedule)

                val response = client.get("/recurrentSchedulesWithMenusInfo/${schedule.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Weekly Lunch"))
            }

        @Test
        fun `returns NotFound for non-existent schedule`() =
            testApplication {
                setupRecurrentSchedulesWithMenusInfoRoutes()

                val response = client.get("/recurrentSchedulesWithMenusInfo/${UUID.randomUUID()}")

                assertEquals(HttpStatusCode.NotFound, response.status)
            }
    }
}
