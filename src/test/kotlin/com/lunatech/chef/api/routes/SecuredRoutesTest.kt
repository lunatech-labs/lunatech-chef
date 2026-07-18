package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.KEYCLOAK_AUTH
import com.lunatech.chef.api.auth.adminOnly
import com.lunatech.chef.api.auth.adminOnlyWrites
import com.lunatech.chef.api.domain.NewAttendance
import com.lunatech.chef.api.domain.NewOffice
import com.lunatech.chef.api.domain.NewUser
import com.lunatech.chef.api.domain.User
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
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Verifies server-side authorization: admin-managed resources reject writes from
 * regular employees, reports are admin-only, and per-user data (profiles, attendances)
 * can only be modified by the owner or an admin.
 */
class SecuredRoutesTest {
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var menusService: MenusService
    private lateinit var schedulesService: SchedulesService
    private lateinit var usersService: UsersService
    private lateinit var attendancesService: AttendancesService
    private lateinit var reportService: ReportService
    private lateinit var excelService: ExcelService

    private lateinit var employee: User
    private lateinit var otherUser: User
    private lateinit var admin: User

    private lateinit var testOfficeUuid: UUID
    private lateinit var testScheduleUuid: UUID
    private lateinit var employeeAttendanceUuid: UUID
    private lateinit var otherUserAttendanceUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        officesService = OfficesService(database)
        dishesService = DishesService(database)
        menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService)
        reportService = ReportService(database)
        excelService = ExcelService()

        val office = anOffice(city = "Rotterdam")
        officesService.insert(office)
        testOfficeUuid = office.uuid

        val dish = aDish(name = "Pasta")
        dishesService.insert(dish)
        val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(dish.uuid))
        menusService.insert(menu)
        val schedule = aSchedule(menuUuid = menu.uuid, officeUuid = testOfficeUuid)
        schedulesService.insert(schedule)
        testScheduleUuid = schedule.uuid

        employee = aUser(name = "Regular Employee", emailAddress = uniqueEmail("employee"), officeUuid = testOfficeUuid)
        otherUser = aUser(name = "Other User", emailAddress = uniqueEmail("other"), officeUuid = testOfficeUuid)
        admin = aUser(name = "Admin User", emailAddress = uniqueEmail("admin"), officeUuid = testOfficeUuid)
        usersService.insert(employee)
        usersService.insert(otherUser)
        usersService.insert(admin)

        val employeeAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = employee.uuid)
        val otherUserAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = otherUser.uuid)
        attendancesService.insert(employeeAttendance)
        attendancesService.insert(otherUserAttendance)
        employeeAttendanceUuid = employeeAttendance.uuid
        otherUserAttendanceUuid = otherUserAttendance.uuid
    }

    private fun ApplicationTestBuilder.setupSecuredRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        installKeycloakAuth(usersService)
        routing {
            authenticate(KEYCLOAK_AUTH) {
                users(usersService)
                attendances(attendancesService)
                adminOnlyWrites {
                    offices(officesService)
                }
                adminOnly {
                    reports(reportService, excelService)
                }
            }
        }
    }

    private fun anUpdatedUser() = UpdatedUser(officeUuid = testOfficeUuid, isVegetarian = true)

    @Nested
    inner class WithoutSession {
        @Test
        fun `rejects requests without a token`() =
            testApplication {
                setupSecuredRoutes()
                val response = client.get("/offices")
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }
    }

    @Nested
    inner class AsEmployee {
        @Test
        fun `can read offices`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response = client.get("/offices")

                assertEquals(HttpStatusCode.OK, response.status)
            }

        @Test
        fun `cannot create an office`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response =
                    client.post("/offices") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(NewOffice(city = "Amsterdam", country = "Netherlands"))
                    }

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `cannot update an office`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response =
                    client.put("/offices/$testOfficeUuid") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(UpdatedOffice(city = "Utrecht", country = "Netherlands"))
                    }

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `cannot delete an office`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response = client.delete("/offices/$testOfficeUuid")

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `cannot download reports`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response = client.get("/reports?year=2026&month=7")

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `cannot list all users`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response = client.get("/users")

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `cannot create a user`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response =
                    client.post("/users") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(NewUser(name = "Intruder", emailAddress = uniqueEmail("intruder"), officeUuid = null))
                    }

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `cannot delete a user`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response = client.delete("/users/${otherUser.uuid}")

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `can read own profile`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response = client.get("/users/${employee.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
            }

        @Test
        fun `cannot read another users profile`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response = client.get("/users/${otherUser.uuid}")

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `can update own profile`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response =
                    client.put("/users/${employee.uuid}") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(anUpdatedUser())
                    }

                assertEquals(HttpStatusCode.OK, response.status)
            }

        @Test
        fun `cannot update another users profile`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response =
                    client.put("/users/${otherUser.uuid}") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(anUpdatedUser())
                    }

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `can sign up own attendance`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response =
                    client.post("/attendances") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(NewAttendance(scheduleUuid = testScheduleUuid, userUuid = employee.uuid, isAttending = true))
                    }

                assertEquals(HttpStatusCode.Created, response.status)
            }

        @Test
        fun `cannot create an attendance for another user`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response =
                    client.post("/attendances") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(NewAttendance(scheduleUuid = testScheduleUuid, userUuid = otherUser.uuid, isAttending = true))
                    }

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }

        @Test
        fun `can update own attendance`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response =
                    client.put("/attendances/$employeeAttendanceUuid") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(UpdatedAttendance(isAttending = true))
                    }

                assertEquals(HttpStatusCode.OK, response.status)
            }

        @Test
        fun `cannot update another users attendance`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(employee))

                val response =
                    client.put("/attendances/$otherUserAttendanceUuid") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(UpdatedAttendance(isAttending = true))
                    }

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }
    }

    @Nested
    inner class AsAdmin {
        @Test
        fun `can create an office`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(admin, isAdmin = true))

                val response =
                    client.post("/offices") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(NewOffice(city = "Amsterdam", country = "Netherlands"))
                    }

                assertEquals(HttpStatusCode.Created, response.status)
            }

        @Test
        fun `can download reports`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(admin, isAdmin = true))

                val response = client.get("/reports?year=2026&month=7")

                assertEquals(HttpStatusCode.OK, response.status)
            }

        @Test
        fun `can list all users`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(admin, isAdmin = true))

                val response = client.get("/users")

                assertEquals(HttpStatusCode.OK, response.status)
            }

        @Test
        fun `can update another users profile`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(admin, isAdmin = true))

                val response =
                    client.put("/users/${otherUser.uuid}") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(anUpdatedUser())
                    }

                assertEquals(HttpStatusCode.OK, response.status)
            }

        @Test
        fun `can update another users attendance`() =
            testApplication {
                setupSecuredRoutes()
                val client = authenticatedJsonClient(aUserToken(admin, isAdmin = true))

                val response =
                    client.put("/attendances/$otherUserAttendanceUuid") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(UpdatedAttendance(isAttending = true))
                    }

                assertEquals(HttpStatusCode.OK, response.status)
            }
    }
}
