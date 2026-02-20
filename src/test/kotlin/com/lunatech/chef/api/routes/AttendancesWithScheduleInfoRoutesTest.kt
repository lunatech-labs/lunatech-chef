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
import com.lunatech.chef.api.persistence.services.AttendancesWithScheduleInfoService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
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

class AttendancesWithScheduleInfoRoutesTest {
    private lateinit var attendancesWithScheduleInfoService: AttendancesWithScheduleInfoService
    private lateinit var attendancesService: AttendancesService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var menusWithDishesNamesService: MenusWithDishesNamesService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var usersService: UsersService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testUserUuid: UUID
    private lateinit var testScheduleUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        dishesService = DishesService(database)
        officesService = OfficesService(database)
        menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        usersService = UsersService(database)
        menusWithDishesNamesService = MenusWithDishesNamesService(database)
        attendancesService = AttendancesService(database, usersService, schedulesService)
        attendancesWithScheduleInfoService = AttendancesWithScheduleInfoService(database, schedulesService, menusWithDishesNamesService)

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

        val testSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
        schedulesService.insert(testSchedule)
        testScheduleUuid = testSchedule.uuid
    }

    private fun ApplicationTestBuilder.setupAttendancesWithScheduleInfoRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { attendancesWithScheduleInfo(attendancesWithScheduleInfoService) }
    }

    @Nested
    inner class GetAttendancesWithScheduleInfo {
        @Test
        fun `returns empty list when no attendances exist`() =
            testApplication {
                setupAttendancesWithScheduleInfoRoutes()

                val response = client.get("/attendancesWithScheduleInfo/$testUserUuid")

                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[]", response.bodyAsText())
            }

        @Test
        fun `returns attendances with schedule info`() =
            testApplication {
                setupAttendancesWithScheduleInfoRoutes()
                val attendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
                attendancesService.insert(attendance)

                val response = client.get("/attendancesWithScheduleInfo/$testUserUuid")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Lunch Menu"))
                assertTrue(response.bodyAsText().contains("Rotterdam"))
            }
    }

    @Nested
    inner class FilteringAttendancesWithScheduleInfo {
        @Test
        fun `filters by fromdate`() =
            testApplication {
                setupAttendancesWithScheduleInfoRoutes()
                val attendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
                attendancesService.insert(attendance)

                val response = client.get("/attendancesWithScheduleInfo/$testUserUuid?fromdate=${LocalDate.now()}")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains(testScheduleUuid.toString()))
            }

        @Test
        fun `filters by office`() =
            testApplication {
                setupAttendancesWithScheduleInfoRoutes()
                val attendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
                attendancesService.insert(attendance)

                val response = client.get("/attendancesWithScheduleInfo/$testUserUuid?office=$testOfficeUuid")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Rotterdam"))
            }
    }
}
