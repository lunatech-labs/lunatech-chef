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
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class AttendancesRoutesTest {
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

        val testUser = aUser(name = "Test User", emailAddress = uniqueEmail("test"), officeUuid = testOfficeUuid)
        usersService.insert(testUser)
        testUserUuid = testUser.uuid

        val testSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
        schedulesService.insert(testSchedule)
        testScheduleUuid = testSchedule.uuid
    }

    private fun ApplicationTestBuilder.setupAttendancesRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { attendances(attendancesService) }
    }

    @Nested
    inner class CreateAttendance {
        @Test
        fun `creates new attendance`() =
            testApplication {
                setupAttendancesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/attendances") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "scheduleUuid" to testScheduleUuid.toString(),
                                "userUuid" to testUserUuid.toString(),
                                "isAttending" to true,
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
            }

        @Test
        fun `creates attendance with false isAttending`() =
            testApplication {
                setupAttendancesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/attendances") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "scheduleUuid" to testScheduleUuid.toString(),
                                "userUuid" to testUserUuid.toString(),
                                "isAttending" to false,
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
            }

        @Test
        fun `creates attendance with null isAttending for undecided`() =
            testApplication {
                setupAttendancesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/attendances") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "scheduleUuid" to testScheduleUuid.toString(),
                                "userUuid" to testUserUuid.toString(),
                                "isAttending" to null,
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
            }

        @Test
        fun `returns BadRequest for invalid data`() =
            testApplication {
                setupAttendancesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/attendances") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(mapOf("invalid" to "data"))
                    }

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }

        @Test
        fun `returns BadRequest for invalid JSON`() =
            testApplication {
                setupAttendancesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/attendances") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody("{ invalid json }")
                    }

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }
    }

    @Nested
    inner class UpdateAttendance {
        @Test
        fun `updates existing attendance`() =
            testApplication {
                setupAttendancesRoutes()
                val client = jsonClient()
                val attendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = false)
                attendancesService.insert(attendance)

                val response =
                    client.put("/attendances/${attendance.uuid}") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(mapOf("isAttending" to true))
                    }

                assertEquals(HttpStatusCode.OK, response.status)
            }

        @Test
        fun `updates attendance from true to false`() =
            testApplication {
                setupAttendancesRoutes()
                val client = jsonClient()
                val attendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
                attendancesService.insert(attendance)

                val response =
                    client.put("/attendances/${attendance.uuid}") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(mapOf("isAttending" to false))
                    }

                assertEquals(HttpStatusCode.OK, response.status)
            }
    }
}
