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
import com.lunatech.chef.api.routes.UpdatedSchedule
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

class SchedulesServiceTest {
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var usersService: UsersService
    private lateinit var attendancesService: AttendancesService
    private lateinit var externalAttendancesService: ExternalAttendancesService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testMenu2Uuid: UUID
    private lateinit var testUserUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        schedulesService = SchedulesService(database)
        menusService = MenusService(database)
        officesService = OfficesService(database)
        dishesService = DishesService(database)
        usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService)
        externalAttendancesService = ExternalAttendancesService(database)

        // Create test office
        val testOffice = anOffice(city = "Rotterdam")
        officesService.insert(testOffice)
        testOfficeUuid = testOffice.uuid

        // Create test dishes and menus
        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)

        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        val testMenu2 = aMenu(name = "Dinner Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        menusService.insert(testMenu2)
        testMenuUuid = testMenu.uuid
        testMenu2Uuid = testMenu2.uuid

        // Create test user
        val testUser = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
        usersService.insert(testUser)
        testUserUuid = testUser.uuid
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
        fun `insert returns 1 when schedule is successfully created`() {
            val schedule =
                aSchedule(
                    menuUuid = testMenuUuid,
                    date = LocalDate.now().plusDays(7),
                    officeUuid = testOfficeUuid,
                )

            val insertResult = schedulesService.insert(schedule)

            assertEquals(1, insertResult, "Insert should return 1 for successful creation")
        }

        @Test
        fun `insert persists all schedule properties correctly`() {
            val scheduleDate = LocalDate.now().plusDays(7)
            val schedule =
                aSchedule(
                    menuUuid = testMenuUuid,
                    date = scheduleDate,
                    officeUuid = testOfficeUuid,
                )

            schedulesService.insert(schedule)
            val retrieved = schedulesService.getByUuid(schedule.uuid)

            assertEquals(1, retrieved.size)
            assertEquals(testMenuUuid, retrieved[0].menuUuid)
            assertEquals(scheduleDate, retrieved[0].date)
            assertEquals(testOfficeUuid, retrieved[0].officeUuid)
        }
    }

    @Nested
    inner class ReadOperations {
        @Test
        fun `getAll returns only non-deleted schedules ordered by date`() {
            val schedule1 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(10), officeUuid = testOfficeUuid)
            val schedule2 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(5), officeUuid = testOfficeUuid)
            val deletedSchedule =
                aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(3), officeUuid = testOfficeUuid, isDeleted = true)

            schedulesService.insert(schedule1)
            schedulesService.insert(schedule2)
            schedulesService.insert(deletedSchedule)

            val allSchedules = schedulesService.getAll()

            assertEquals(2, allSchedules.size, "Should return only non-deleted schedules")
            assertTrue(allSchedules.none { it.uuid == deletedSchedule.uuid }, "Deleted schedule should not be in results")
            // Verify ordering by date ascending
            assertEquals(schedule2.uuid, allSchedules[0].uuid, "Earlier schedule should be first")
            assertEquals(schedule1.uuid, allSchedules[1].uuid, "Later schedule should be second")
        }

        @Test
        fun `getAll returns empty list when no schedules exist`() {
            val allSchedules = schedulesService.getAll()

            assertTrue(allSchedules.isEmpty(), "Should return empty list when no schedules exist")
        }

        @Test
        fun `getByUuid returns schedule when it exists`() {
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val retrieved = schedulesService.getByUuid(schedule.uuid)

            assertEquals(1, retrieved.size, "Should return exactly one schedule")
            assertEquals(schedule.uuid, retrieved[0].uuid)
        }

        @Test
        fun `getByUuid returns empty list for non-existent UUID`() {
            val nonExistentUuid = UUID.randomUUID()

            val result = schedulesService.getByUuid(nonExistentUuid)

            assertTrue(result.isEmpty(), "Should return empty list for non-existent UUID")
        }

        @Test
        fun `getByUuid returns deleted schedule without filtering`() {
            val deletedSchedule =
                aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid, isDeleted = true)
            schedulesService.insert(deletedSchedule)

            val retrieved = schedulesService.getByUuid(deletedSchedule.uuid)

            assertEquals(1, retrieved.size, "getByUuid should return deleted schedules")
            assertTrue(retrieved[0].isDeleted)
        }

        @Test
        fun `getAfterDate returns schedules on or after specified date`() {
            val pastSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(5), officeUuid = testOfficeUuid)
            val futureSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(5), officeUuid = testOfficeUuid)
            val todaySchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now(), officeUuid = testOfficeUuid)

            schedulesService.insert(pastSchedule)
            schedulesService.insert(futureSchedule)
            schedulesService.insert(todaySchedule)

            val schedulesAfterToday = schedulesService.getAfterDate(LocalDate.now())

            assertEquals(2, schedulesAfterToday.size, "Should return today's and future schedules")
            assertTrue(schedulesAfterToday.any { it.uuid == todaySchedule.uuid }, "Today's schedule should be included")
            assertTrue(schedulesAfterToday.any { it.uuid == futureSchedule.uuid }, "Future schedule should be included")
        }

        @Test
        fun `getAfterDate returns all schedules including deleted ones`() {
            // Note: getAfterDate does NOT filter by isDeleted - it returns all schedules >= date
            val activeSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(5), officeUuid = testOfficeUuid)
            val deletedSchedule =
                aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(6), officeUuid = testOfficeUuid, isDeleted = true)

            schedulesService.insert(activeSchedule)
            schedulesService.insert(deletedSchedule)

            val schedules = schedulesService.getAfterDate(LocalDate.now())

            assertEquals(2, schedules.size, "getAfterDate should return both active and deleted schedules")
        }
    }

    @Nested
    inner class UpdateOperations {
        @Test
        fun `update returns 1 when schedule is successfully updated`() {
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val updatedSchedule =
                UpdatedSchedule(
                    menuUuid = testMenu2Uuid,
                    date = LocalDate.now().plusDays(14),
                    officeUuid = testOfficeUuid,
                )

            val updateResult = schedulesService.update(schedule.uuid, updatedSchedule)

            assertEquals(1, updateResult, "Update should return 1 for successful update")
        }

        @Test
        fun `update modifies schedule properties`() {
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val newDate = LocalDate.now().plusDays(14)
            val updatedSchedule =
                UpdatedSchedule(
                    menuUuid = testMenu2Uuid,
                    date = newDate,
                    officeUuid = testOfficeUuid,
                )
            schedulesService.update(schedule.uuid, updatedSchedule)

            val retrieved = schedulesService.getByUuid(schedule.uuid)[0]

            assertEquals(testMenu2Uuid, retrieved.menuUuid)
            assertEquals(newDate, retrieved.date)
        }

        @Test
        fun `update returns 0 for non-existent schedule`() {
            val nonExistentUuid = UUID.randomUUID()
            val updatedSchedule =
                UpdatedSchedule(
                    menuUuid = testMenuUuid,
                    date = LocalDate.now().plusDays(7),
                    officeUuid = testOfficeUuid,
                )

            val updateResult = schedulesService.update(nonExistentUuid, updatedSchedule)

            assertEquals(0, updateResult, "Update should return 0 for non-existent schedule")
        }
    }

    @Nested
    inner class DeleteOperations {
        @Test
        fun `delete performs soft delete by setting isDeleted flag`() {
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            schedulesService.delete(schedule.uuid)

            val retrieved = schedulesService.getByUuid(schedule.uuid)
            assertEquals(1, retrieved.size, "Schedule should still exist in database")
            assertTrue(retrieved[0].isDeleted, "isDeleted flag should be set to true")
        }

        @Test
        fun `delete removes schedule from getAll results`() {
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            schedulesService.delete(schedule.uuid)

            val allSchedules = schedulesService.getAll()
            assertTrue(allSchedules.isEmpty(), "Deleted schedule should not appear in getAll")
        }

        @Test
        fun `delete returns 0 for non-existent schedule`() {
            val nonExistentUuid = UUID.randomUUID()

            val deleteResult = schedulesService.delete(nonExistentUuid)

            assertEquals(0, deleteResult, "Delete should return 0 for non-existent schedule")
        }

        @Test
        fun `delete soft deletes related attendances`() {
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)
            val attendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(attendance)

            schedulesService.delete(schedule.uuid)

            val attendances = getAttendancesByScheduleUuid(schedule.uuid)
            assertEquals(1, attendances.size, "Attendance should still exist in database")
            assertTrue(attendances[0].isDeleted, "Related attendance should be soft deleted")
        }

        @Test
        fun `delete soft deletes related external attendances`() {
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)
            val externalAttendance = anExternalAttendance(scheduleUuid = schedule.uuid, attendancesCount = 5)
            externalAttendancesService.insert(externalAttendance)

            schedulesService.delete(schedule.uuid)

            val externalAttendances = getExternalAttendancesByScheduleUuid(schedule.uuid)
            assertEquals(1, externalAttendances.size, "External attendance should still exist in database")
            assertTrue(externalAttendances[0].isDeleted, "Related external attendance should be soft deleted")
        }

        @Test
        fun `delete does not affect attendances of other schedules`() {
            val schedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            val otherSchedule = aSchedule(menuUuid = testMenu2Uuid, date = LocalDate.now().plusDays(8), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)
            schedulesService.insert(otherSchedule)

            val attendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUserUuid, isAttending = true)
            val otherAttendance = anAttendance(scheduleUuid = otherSchedule.uuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(attendance)
            attendancesService.insert(otherAttendance)

            val externalAttendance = anExternalAttendance(scheduleUuid = schedule.uuid, attendancesCount = 2)
            val otherExternalAttendance = anExternalAttendance(scheduleUuid = otherSchedule.uuid, attendancesCount = 3)
            externalAttendancesService.insert(externalAttendance)
            externalAttendancesService.insert(otherExternalAttendance)

            schedulesService.delete(schedule.uuid)

            assertFalse(
                schedulesService.getByUuid(otherSchedule.uuid)[0].isDeleted,
                "Other schedule should not be soft deleted",
            )
            assertFalse(
                getAttendancesByScheduleUuid(otherSchedule.uuid)[0].isDeleted,
                "Attendance of other schedule should not be soft deleted",
            )
            assertFalse(
                getExternalAttendancesByScheduleUuid(otherSchedule.uuid)[0].isDeleted,
                "External attendance of other schedule should not be soft deleted",
            )
        }
    }
}
