package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesWithMenuInfoService
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

class SchedulesWithMenusInfoRoutesTest {
    private lateinit var schedulesWithMenuInfoService: SchedulesWithMenuInfoService
    private lateinit var schedulesService: SchedulesService
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
        schedulesService = SchedulesService(database)
        menusWithDishesNamesService = MenusWithDishesNamesService(database)
        schedulesWithMenuInfoService = SchedulesWithMenuInfoService(database, menusWithDishesNamesService)

        val testOffice = anOffice(city = "Rotterdam")
        val testOffice2 = anOffice(city = "Paris", country = "France")
        officesService.insert(testOffice)
        officesService.insert(testOffice2)
        testOfficeUuid = testOffice.uuid
        testOffice2Uuid = testOffice2.uuid

        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)

        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid
    }

    private fun ApplicationTestBuilder.setupSchedulesWithMenusInfoRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { schedulesWithMenusInfo(schedulesWithMenuInfoService) }
    }

    @Nested
    inner class GetAllSchedulesWithMenusInfo {
        @Test
        fun `returns empty list when no schedules exist`() = testApplication {
            setupSchedulesWithMenusInfoRoutes()

            val response = client.get("/schedulesWithMenusInfo")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

        @Test
        fun `returns schedules with menu and office info`() = testApplication {
            setupSchedulesWithMenusInfoRoutes()
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val response = client.get("/schedulesWithMenusInfo")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Lunch Menu"))
            assertTrue(response.bodyAsText().contains("Rotterdam"))
        }
    }

    @Nested
    inner class FilteringSchedules {
        @Test
        fun `filters by fromdate`() = testApplication {
            setupSchedulesWithMenusInfoRoutes()
            val pastSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(5), officeUuid = testOfficeUuid)
            val futureSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(5), officeUuid = testOfficeUuid)
            schedulesService.insert(pastSchedule)
            schedulesService.insert(futureSchedule)

            val response = client.get("/schedulesWithMenusInfo?fromdate=${LocalDate.now()}")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains(futureSchedule.uuid.toString()))
        }

        @Test
        fun `filters by office`() = testApplication {
            setupSchedulesWithMenusInfoRoutes()
            val schedule1 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            val schedule2 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(8), officeUuid = testOffice2Uuid)
            schedulesService.insert(schedule1)
            schedulesService.insert(schedule2)

            val response = client.get("/schedulesWithMenusInfo?office=$testOfficeUuid")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Rotterdam"))
        }
    }

    @Nested
    inner class GetScheduleWithMenusInfoByUuid {
        @Test
        fun `returns schedule with info`() = testApplication {
            setupSchedulesWithMenusInfoRoutes()
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val response = client.get("/schedulesWithMenusInfo/${schedule.uuid}")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Lunch Menu"))
        }

        @Test
        fun `returns NotFound for non-existent schedule`() = testApplication {
            setupSchedulesWithMenusInfoRoutes()

            val response = client.get("/schedulesWithMenusInfo/${UUID.randomUUID()}")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    }
}
