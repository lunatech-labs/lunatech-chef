package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import java.time.LocalDate
import java.util.UUID

class SlackInteractionRoutesTest {
    private val publicUrl = "https://lunch.lunatech.nl"

    private lateinit var attendancesService: AttendancesService
    private lateinit var attendanceUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        val officesService = OfficesService(database)
        val dishesService = DishesService(database)
        val menusService = MenusService(database)
        val schedulesService = SchedulesService(database)
        val usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService)

        val office = anOffice(city = "Rotterdam")
        officesService.insert(office)
        val dish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(dish)
        val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(dish.uuid))
        menusService.insert(menu)
        val user = aUser(name = "Clicker", emailAddress = uniqueEmail("clicker"), officeUuid = office.uuid)
        usersService.insert(user)
        val schedule = aSchedule(menuUuid = menu.uuid, date = LocalDate.now().plusDays(1), officeUuid = office.uuid)
        schedulesService.insert(schedule)
        val attendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = user.uuid, isAttending = null)
        attendancesService.insert(attendance)
        attendanceUuid = attendance.uuid
    }

    private fun ApplicationTestBuilder.setupSlackRoute() {
        routing { slackInteraction(attendancesService, publicUrl) }
    }

    // Same direct-query helper pattern as AttendancesServiceTest (the service has no read method)
    private fun getAttendanceByUuid(uuid: UUID): Attendance? {
        val database = TestDatabase.getDatabase()
        return database
            .from(Attendances)
            .select()
            .where { Attendances.uuid eq uuid }
            .map { row ->
                Attendance(
                    uuid = row[Attendances.uuid]!!,
                    scheduleUuid = row[Attendances.scheduleUuid]!!,
                    userUuid = row[Attendances.userUuid]!!,
                    isAttending = row[Attendances.isAttending],
                    isDeleted = row[Attendances.isDeleted]!!,
                )
            }.firstOrNull()
    }

    private fun payload(
        value: String,
        callbackId: String = "Rotterdam_Monday",
    ): String = """{"callback_id":"$callbackId","actions":[{"name":"decision","type":"button","value":"$value"}],"user":{"id":"U1"}}"""

    @Test
    fun `yes answer updates attendance and responds with going ack`() =
        testApplication {
            setupSlackRoute()

            val response =
                client.submitForm(
                    url = "/slack",
                    formParameters = parameters { append("payload", payload("${attendanceUuid}_true")) },
                )

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                "Great, see you in Rotterdam on Monday! :star-struck: If your plans change, please update your answer at $publicUrl.",
                response.bodyAsText(),
            )
            assertEquals(true, getAttendanceByUuid(attendanceUuid)?.isAttending)
        }

    @Test
    fun `no answer updates attendance and responds with not going ack`() =
        testApplication {
            setupSlackRoute()

            val response =
                client.submitForm(
                    url = "/slack",
                    formParameters = parameters { append("payload", payload("${attendanceUuid}_false")) },
                )

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                "Alright :cry: If your plans change, please update your answer at $publicUrl.",
                response.bodyAsText(),
            )
            assertEquals(false, getAttendanceByUuid(attendanceUuid)?.isAttending)
        }

    @Test
    fun `malformed payload responds with error ack`() =
        testApplication {
            setupSlackRoute()

            val response =
                client.submitForm(
                    url = "/slack",
                    formParameters = parameters { append("payload", "{not json") },
                )

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().startsWith("Something went wrong"))
        }

    @Test
    fun `unknown attendance uuid responds with error ack`() =
        testApplication {
            setupSlackRoute()

            val response =
                client.submitForm(
                    url = "/slack",
                    formParameters = parameters { append("payload", payload("${UUID.randomUUID()}_true")) },
                )

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().startsWith("Something went wrong"))
        }

    @Test
    fun `malformed callback id still updates but responds with error ack`() =
        testApplication {
            setupSlackRoute()

            val response =
                client.submitForm(
                    url = "/slack",
                    formParameters = parameters { append("payload", payload("${attendanceUuid}_true", callbackId = "nodash")) },
                )

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().startsWith("Something went wrong"))
            assertEquals(true, getAttendanceByUuid(attendanceUuid)?.isAttending)
        }

    @Test
    fun `office name containing an underscore still gets the going ack`() =
        testApplication {
            setupSlackRoute()

            val response =
                client.submitForm(
                    url = "/slack",
                    formParameters = parameters { append("payload", payload("${attendanceUuid}_true", callbackId = "Den_Haag_Monday")) },
                )

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                "Great, see you in Den_Haag on Monday! :star-struck: If your plans change, please update your answer at $publicUrl.",
                response.bodyAsText(),
            )
            assertEquals(true, getAttendanceByUuid(attendanceUuid)?.isAttending)
        }

    @Test
    fun `a 3-part action value responds with error ack and does not update the attendance`() =
        testApplication {
            setupSlackRoute()

            val response =
                client.submitForm(
                    url = "/slack",
                    formParameters = parameters { append("payload", payload("${attendanceUuid}_true_extra")) },
                )

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().startsWith("Something went wrong"))
            assertEquals(null, getAttendanceByUuid(attendanceUuid)?.isAttending)
        }

    @Test
    fun `a payload with no decision-named action responds with error ack`() =
        testApplication {
            setupSlackRoute()

            val noDecisionPayload =
                """{"callback_id":"Rotterdam_Monday","actions":[{"name":"other","type":"button","value":"${attendanceUuid}_true"}],"user":{"id":"U1"}}"""
            val response =
                client.submitForm(
                    url = "/slack",
                    formParameters = parameters { append("payload", noDecisionPayload) },
                )

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().startsWith("Something went wrong"))
            assertEquals(null, getAttendanceByUuid(attendanceUuid)?.isAttending)
        }
}
