package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.schemas.Attendances
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import java.time.LocalDate
import java.util.UUID

class UsersServiceProvisionTest {
    private lateinit var database: Database
    private lateinit var usersService: UsersService
    private lateinit var schedulesService: SchedulesService

    private lateinit var futureScheduleUuid: UUID

    @BeforeEach
    fun setup() {
        database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        usersService = UsersService(database)
        schedulesService = SchedulesService(database)

        val officesService = OfficesService(database)
        val dishesService = DishesService(database)
        val menusService = MenusService(database)

        val office = anOffice(city = "Rotterdam")
        officesService.insert(office)
        val dish = aDish(name = "Pasta")
        dishesService.insert(dish)
        val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(dish.uuid))
        menusService.insert(menu)

        val pastSchedule = aSchedule(menuUuid = menu.uuid, officeUuid = office.uuid, date = LocalDate.now().minusDays(7))
        val futureSchedule = aSchedule(menuUuid = menu.uuid, officeUuid = office.uuid, date = LocalDate.now().plusDays(7))
        schedulesService.insert(pastSchedule)
        schedulesService.insert(futureSchedule)
        futureScheduleUuid = futureSchedule.uuid
    }

    private fun attendanceScheduleUuidsFor(userUuid: UUID): List<UUID> =
        database
            .from(Attendances)
            .select(Attendances.scheduleUuid)
            .where { Attendances.userUuid eq userUuid }
            .map { row -> row[Attendances.scheduleUuid]!! }

    @Nested
    inner class Provision {
        @Test
        fun `creates a user with a name derived from the email`() {
            val user = usersService.provision("john.doe@lunatech.nl")

            assertEquals("John Doe", user.name)
            assertEquals("john.doe@lunatech.nl", user.emailAddress)
            assertEquals(user.uuid, usersService.getByEmailAddress("john.doe@lunatech.nl")?.uuid)
        }

        @Test
        fun `enrols the new user into schedules from today onward only`() {
            val user = usersService.provision(uniqueEmail("newbie"))

            assertEquals(listOf(futureScheduleUuid), attendanceScheduleUuidsFor(user.uuid))
        }

        @Test
        fun `returns the existing user without touching enrolments`() {
            val existing = aUser(name = "Existing User", emailAddress = uniqueEmail("existing"))
            usersService.insert(existing)

            val user = usersService.provision(existing.emailAddress)

            assertEquals(existing.uuid, user.uuid)
            assertEquals(emptyList<UUID>(), attendanceScheduleUuidsFor(existing.uuid))
        }

        @Test
        fun `is idempotent when called twice`() {
            val email = uniqueEmail("twice")

            val first = usersService.provision(email)
            val second = usersService.provision(email)

            assertEquals(first.uuid, second.uuid)
            assertEquals(1, attendanceScheduleUuidsFor(first.uuid).size)
        }
    }

    @Nested
    inner class UniqueEmailIndex {
        @Test
        fun `rejects a second user with the same email address`() {
            val email = uniqueEmail("dup")
            usersService.insert(aUser(name = "First", emailAddress = email))

            assertThrows(Exception::class.java) {
                usersService.insert(aUser(name = "Second", emailAddress = email))
            }
        }
    }

    @Nested
    inner class GetUserNameFromEmail {
        @Test
        fun `extracts and formats name correctly`() {
            assertEquals("John Doe", getUserNameFromEmail("john.doe@lunatech.nl"))
        }

        @Test
        fun `handles single name`() {
            assertEquals("Admin", getUserNameFromEmail("admin@lunatech.nl"))
        }

        @Test
        fun `handles multiple parts`() {
            assertEquals("John Middle Doe", getUserNameFromEmail("john.middle.doe@lunatech.nl"))
        }
    }
}
