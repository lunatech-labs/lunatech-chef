package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anExternalAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.ExternalAttendances
import com.lunatech.chef.api.routes.UpdatedOffice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import java.time.LocalDate
import java.util.UUID

class OfficesServiceTest {
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var menusService: MenusService
    private lateinit var schedulesService: SchedulesService
    private lateinit var usersService: UsersService
    private lateinit var attendancesService: AttendancesService
    private lateinit var externalAttendancesService: ExternalAttendancesService

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
        externalAttendancesService = ExternalAttendancesService(database)
    }

    // Helper to create a menu for schedule tests
    private fun createTestMenu(): UUID {
        val dish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(dish)
        val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(dish.uuid))
        menusService.insert(menu)
        return menu.uuid
    }

    // Helper to retrieve attendances directly from database, including deleted ones
    private fun getAttendancesByScheduleUuid(scheduleUuid: UUID) =
        TestDatabase
            .getDatabase()
            .from(Attendances)
            .select()
            .where { Attendances.scheduleUuid eq scheduleUuid }
            .map { Attendances.createEntity(it) }

    // Helper to retrieve external attendances directly from database, including deleted ones
    private fun getExternalAttendancesByScheduleUuid(scheduleUuid: UUID) =
        TestDatabase
            .getDatabase()
            .from(ExternalAttendances)
            .select()
            .where { ExternalAttendances.scheduleUuid eq scheduleUuid }
            .map { ExternalAttendances.createEntity(it) }

    @Nested
    inner class InsertOperations {
        @Test
        fun `insert returns 1 when office is successfully created`() {
            val office = anOffice(city = "Rotterdam", country = "Netherlands")

            val insertResult = officesService.insert(office)

            assertEquals(1, insertResult, "Insert should return 1 for successful creation")
        }

        @Test
        fun `insert persists all office properties correctly`() {
            val office = anOffice(city = "Rotterdam", country = "Netherlands")

            officesService.insert(office)
            val retrieved = officesService.getByUuid(office.uuid)

            assertEquals(1, retrieved.size)
            assertEquals(office.city, retrieved[0].city)
            assertEquals(office.country, retrieved[0].country)
        }
    }

    @Nested
    inner class ReadOperations {
        @Test
        fun `getAll returns only non-deleted offices`() {
            val office1 = anOffice(city = "Rotterdam")
            val office2 = anOffice(city = "Paris", country = "France")
            val deletedOffice = anOffice(city = "Berlin", country = "Germany", isDeleted = true)

            officesService.insert(office1)
            officesService.insert(office2)
            officesService.insert(deletedOffice)

            val allOffices = officesService.getAll()

            assertEquals(2, allOffices.size, "Should return only non-deleted offices")
            assertTrue(allOffices.none { it.uuid == deletedOffice.uuid }, "Deleted office should not be in results")
        }

        @Test
        fun `getAll returns empty list when no offices exist`() {
            val allOffices = officesService.getAll()

            assertTrue(allOffices.isEmpty(), "Should return empty list when no offices exist")
        }

        @Test
        fun `getByUuid returns office when it exists`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)

            val retrieved = officesService.getByUuid(office.uuid)

            assertEquals(1, retrieved.size, "Should return exactly one office")
            assertEquals(office.uuid, retrieved[0].uuid)
        }

        @Test
        fun `getByUuid returns empty list for non-existent UUID`() {
            val nonExistentUuid = UUID.randomUUID()

            val result = officesService.getByUuid(nonExistentUuid)

            assertTrue(result.isEmpty(), "Should return empty list for non-existent UUID")
        }

        @Test
        fun `getByUuid returns deleted office without filtering`() {
            val deletedOffice = anOffice(city = "Deleted City", isDeleted = true)
            officesService.insert(deletedOffice)

            val retrieved = officesService.getByUuid(deletedOffice.uuid)

            assertEquals(1, retrieved.size, "getByUuid should return deleted offices")
            assertTrue(retrieved[0].isDeleted)
        }
    }

    @Nested
    inner class UpdateOperations {
        @Test
        fun `update returns 1 when office is successfully updated`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)

            val updatedOffice = UpdatedOffice(city = "Amsterdam", country = "Netherlands")

            val updateResult = officesService.update(office.uuid, updatedOffice)

            assertEquals(1, updateResult, "Update should return 1 for successful update")
        }

        @Test
        fun `update modifies office properties`() {
            val office = anOffice(city = "Rotterdam", country = "Netherlands")
            officesService.insert(office)

            val updatedOffice = UpdatedOffice(city = "Amsterdam", country = "Netherlands")
            officesService.update(office.uuid, updatedOffice)

            val retrieved = officesService.getByUuid(office.uuid)[0]

            assertEquals("Amsterdam", retrieved.city)
            assertEquals("Netherlands", retrieved.country)
        }

        @Test
        fun `update returns 0 for non-existent office`() {
            val nonExistentUuid = UUID.randomUUID()
            val updatedOffice = UpdatedOffice(city = "Amsterdam", country = "Netherlands")

            val updateResult = officesService.update(nonExistentUuid, updatedOffice)

            assertEquals(0, updateResult, "Update should return 0 for non-existent office")
        }
    }

    @Nested
    inner class DeleteOperations {
        @Test
        fun `delete performs soft delete by setting isDeleted flag`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)

            officesService.delete(office.uuid)

            val retrieved = officesService.getByUuid(office.uuid)
            assertEquals(1, retrieved.size, "Office should still exist in database")
            assertTrue(retrieved[0].isDeleted, "isDeleted flag should be set to true")
        }

        @Test
        fun `delete removes office from getAll results`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)

            officesService.delete(office.uuid)

            val allOffices = officesService.getAll()
            assertTrue(allOffices.isEmpty(), "Deleted office should not appear in getAll")
        }

        @Test
        fun `delete returns 0 for non-existent office`() {
            val nonExistentUuid = UUID.randomUUID()

            val deleteResult = officesService.delete(nonExistentUuid)

            assertEquals(0, deleteResult, "Delete should return 0 for non-existent office")
        }

        @Test
        fun `delete soft deletes future schedules of the office`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)
            val menuUuid = createTestMenu()
            val futureSchedule = aSchedule(menuUuid = menuUuid, date = LocalDate.now().plusDays(7), officeUuid = office.uuid)
            schedulesService.insert(futureSchedule)

            officesService.delete(office.uuid)

            val retrieved = schedulesService.getByUuid(futureSchedule.uuid)
            assertEquals(1, retrieved.size, "Schedule should still exist in database")
            assertTrue(retrieved[0].isDeleted, "Future schedule of deleted office should be soft deleted")
        }

        @Test
        fun `delete soft deletes attendances and external attendances of future schedules`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)
            val menuUuid = createTestMenu()
            val futureSchedule = aSchedule(menuUuid = menuUuid, date = LocalDate.now().plusDays(7), officeUuid = office.uuid)
            schedulesService.insert(futureSchedule)

            val user = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = office.uuid)
            usersService.insert(user)
            val attendance = anAttendance(scheduleUuid = futureSchedule.uuid, userUuid = user.uuid, isAttending = true)
            attendancesService.insert(attendance)
            val externalAttendance = anExternalAttendance(scheduleUuid = futureSchedule.uuid, attendancesCount = 5)
            externalAttendancesService.insert(externalAttendance)

            officesService.delete(office.uuid)

            val attendances = getAttendancesByScheduleUuid(futureSchedule.uuid)
            assertEquals(1, attendances.size, "Attendance should still exist in database")
            assertTrue(attendances[0].isDeleted, "Attendance of future schedule should be soft deleted")

            val externalAttendances = getExternalAttendancesByScheduleUuid(futureSchedule.uuid)
            assertEquals(1, externalAttendances.size, "External attendance should still exist in database")
            assertTrue(externalAttendances[0].isDeleted, "External attendance of future schedule should be soft deleted")
        }

        @Test
        fun `delete does not soft delete past schedules and their attendances`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)
            val menuUuid = createTestMenu()
            val pastSchedule = aSchedule(menuUuid = menuUuid, date = LocalDate.now().minusDays(5), officeUuid = office.uuid)
            schedulesService.insert(pastSchedule)

            val user = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = office.uuid)
            usersService.insert(user)
            val pastAttendance = anAttendance(scheduleUuid = pastSchedule.uuid, userUuid = user.uuid, isAttending = true)
            attendancesService.insert(pastAttendance)
            val pastExternalAttendance = anExternalAttendance(scheduleUuid = pastSchedule.uuid, attendancesCount = 3)
            externalAttendancesService.insert(pastExternalAttendance)

            officesService.delete(office.uuid)

            assertFalse(
                schedulesService.getByUuid(pastSchedule.uuid)[0].isDeleted,
                "Past schedule should not be soft deleted",
            )
            assertFalse(
                getAttendancesByScheduleUuid(pastSchedule.uuid)[0].isDeleted,
                "Attendance of past schedule should not be soft deleted",
            )
            assertFalse(
                getExternalAttendancesByScheduleUuid(pastSchedule.uuid)[0].isDeleted,
                "External attendance of past schedule should not be soft deleted",
            )
        }

        @Test
        fun `delete does not affect schedules of other offices`() {
            val office = anOffice(city = "Rotterdam")
            val otherOffice = anOffice(city = "Paris", country = "France")
            officesService.insert(office)
            officesService.insert(otherOffice)
            val menuUuid = createTestMenu()

            val otherSchedule = aSchedule(menuUuid = menuUuid, date = LocalDate.now().plusDays(7), officeUuid = otherOffice.uuid)
            schedulesService.insert(otherSchedule)
            val externalAttendance = anExternalAttendance(scheduleUuid = otherSchedule.uuid, attendancesCount = 2)
            externalAttendancesService.insert(externalAttendance)

            officesService.delete(office.uuid)

            assertFalse(
                officesService.getByUuid(otherOffice.uuid)[0].isDeleted,
                "Other office should not be soft deleted",
            )
            assertFalse(
                schedulesService.getByUuid(otherSchedule.uuid)[0].isDeleted,
                "Schedule of other office should not be soft deleted",
            )
            assertFalse(
                getExternalAttendancesByScheduleUuid(otherSchedule.uuid)[0].isDeleted,
                "External attendance of other office's schedule should not be soft deleted",
            )
        }
    }
}
