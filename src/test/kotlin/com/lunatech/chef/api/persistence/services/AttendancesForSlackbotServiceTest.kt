package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class AttendancesForSlackbotServiceTest {
    private lateinit var attendancesForSlackbotService: AttendancesForSlackbotService
    private lateinit var attendancesService: AttendancesService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var usersService: UsersService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testUserUuid: UUID
    private lateinit var testUser2Uuid: UUID
    private lateinit var testScheduleUuid: UUID
    private lateinit var testUserEmail: String

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()

        attendancesForSlackbotService = AttendancesForSlackbotService(database)
        officesService = OfficesService(database)
        dishesService = DishesService(database)
        menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService, schedulesService)

        // Create test office
        val testOffice = anOffice(city = "Rotterdam")
        officesService.insert(testOffice)
        testOfficeUuid = testOffice.uuid

        // Create test users
        testUserEmail = uniqueEmail("alice")
        val testUser = aUser(name = "Alice Smith", emailAddress = testUserEmail, officeUuid = testOfficeUuid)
        val testUser2 = aUser(name = "Bob Jones", emailAddress = uniqueEmail("bob"), officeUuid = testOfficeUuid)
        usersService.insert(testUser)
        usersService.insert(testUser2)
        testUserUuid = testUser.uuid
        testUser2Uuid = testUser2.uuid

        // Create test dish and menu
        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)
        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid

        // Create test schedule
        val testSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(3), officeUuid = testOfficeUuid)
        schedulesService.insert(testSchedule)
        testScheduleUuid = testSchedule.uuid
    }

    @Nested
    inner class GetMissingAttendancesOperations {
        @Test
        fun `getMissingAttendances returns attendances with null isAttending`() {
            val missingAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = null)
            attendancesService.insert(missingAttendance)

            val fromDate = LocalDate.now()
            val untilDate = LocalDate.now().plusDays(7)

            val missing = attendancesForSlackbotService.getMissingAttendances(fromDate, untilDate)

            assertEquals(1, missing.size)
            val entry = missing[0]
            assertEquals(missingAttendance.uuid, entry.attendanceUuid)
            assertEquals(testUserEmail, entry.emailAddress)
            assertEquals("Rotterdam", entry.office)
            assertEquals("Lunch Menu", entry.menuName)
        }

        @Test
        fun `getMissingAttendances excludes answered attendances`() {
            val missingAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = null)
            val answeredAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUser2Uuid, isAttending = true)
            attendancesService.insert(missingAttendance)
            attendancesService.insert(answeredAttendance)

            val fromDate = LocalDate.now()
            val untilDate = LocalDate.now().plusDays(7)

            val missing = attendancesForSlackbotService.getMissingAttendances(fromDate, untilDate)

            assertEquals(1, missing.size)
            assertEquals(testUserEmail, missing[0].emailAddress)
        }

        @Test
        fun `getMissingAttendances excludes deleted schedules`() {
            val deletedSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(5), officeUuid = testOfficeUuid, isDeleted = true)
            schedulesService.insert(deletedSchedule)

            val missingAttendance = anAttendance(scheduleUuid = deletedSchedule.uuid, userUuid = testUserUuid, isAttending = null)
            attendancesService.insert(missingAttendance)

            val fromDate = LocalDate.now()
            val untilDate = LocalDate.now().plusDays(7)

            val missing = attendancesForSlackbotService.getMissingAttendances(fromDate, untilDate)

            assertTrue(missing.isEmpty(), "Should exclude attendances for deleted schedules")
        }

        @Test
        fun `getMissingAttendances excludes inactive users`() {
            val inactiveUser = aUser(name = "Inactive User", emailAddress = uniqueEmail("inactive"), officeUuid = testOfficeUuid, isInactive = true)
            usersService.insert(inactiveUser)

            val missingAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = inactiveUser.uuid, isAttending = null)
            attendancesService.insert(missingAttendance)

            val fromDate = LocalDate.now()
            val untilDate = LocalDate.now().plusDays(7)

            val missing = attendancesForSlackbotService.getMissingAttendances(fromDate, untilDate)

            assertTrue(missing.isEmpty(), "Should exclude inactive users")
        }

        @Test
        fun `getMissingAttendances excludes deleted users`() {
            val deletedUser = aUser(name = "Deleted User", emailAddress = uniqueEmail("deleted"), officeUuid = testOfficeUuid, isDeleted = true)
            usersService.insert(deletedUser)

            val missingAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = deletedUser.uuid, isAttending = null)
            attendancesService.insert(missingAttendance)

            val fromDate = LocalDate.now()
            val untilDate = LocalDate.now().plusDays(7)

            val missing = attendancesForSlackbotService.getMissingAttendances(fromDate, untilDate)

            assertTrue(missing.isEmpty(), "Should exclude deleted users")
        }

        @Test
        fun `getMissingAttendances filters by date range`() {
            val scheduleInRange = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(3), officeUuid = testOfficeUuid)
            val scheduleOutOfRange = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(10), officeUuid = testOfficeUuid)
            schedulesService.insert(scheduleInRange)
            schedulesService.insert(scheduleOutOfRange)

            val attendanceInRange = anAttendance(scheduleUuid = scheduleInRange.uuid, userUuid = testUserUuid, isAttending = null)
            val attendanceOutOfRange = anAttendance(scheduleUuid = scheduleOutOfRange.uuid, userUuid = testUser2Uuid, isAttending = null)
            attendancesService.insert(attendanceInRange)
            attendancesService.insert(attendanceOutOfRange)

            val fromDate = LocalDate.now()
            val untilDate = LocalDate.now().plusDays(5)

            val missing = attendancesForSlackbotService.getMissingAttendances(fromDate, untilDate)

            assertEquals(1, missing.size)
            assertEquals(scheduleInRange.date, missing[0].date)
        }

        @Test
        fun `getMissingAttendances returns results ordered by date`() {
            val schedule1 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(5), officeUuid = testOfficeUuid)
            val schedule2 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(2), officeUuid = testOfficeUuid)
            schedulesService.insert(schedule1)
            schedulesService.insert(schedule2)

            val attendance1 = anAttendance(scheduleUuid = schedule1.uuid, userUuid = testUserUuid, isAttending = null)
            val attendance2 = anAttendance(scheduleUuid = schedule2.uuid, userUuid = testUserUuid, isAttending = null)
            attendancesService.insert(attendance1)
            attendancesService.insert(attendance2)

            val fromDate = LocalDate.now()
            val untilDate = LocalDate.now().plusDays(7)

            val missing = attendancesForSlackbotService.getMissingAttendances(fromDate, untilDate)

            assertEquals(2, missing.size)
            // Results should be ordered by date ascending
            assertEquals(schedule2.date, missing[0].date) // Earlier date first
            assertEquals(schedule1.date, missing[1].date) // Later date second
        }

        @Test
        fun `getMissingAttendances returns empty list when no missing attendances`() {
            val fromDate = LocalDate.now()
            val untilDate = LocalDate.now().plusDays(7)

            val missing = attendancesForSlackbotService.getMissingAttendances(fromDate, untilDate)

            assertTrue(missing.isEmpty())
        }

        @Test
        fun `getMissingAttendances includes schedules on boundary dates`() {
            val fromDate = LocalDate.now()
            val untilDate = LocalDate.now().plusDays(7)

            val scheduleOnFromDate = aSchedule(menuUuid = testMenuUuid, date = fromDate, officeUuid = testOfficeUuid)
            val scheduleOnUntilDate = aSchedule(menuUuid = testMenuUuid, date = untilDate, officeUuid = testOfficeUuid)
            schedulesService.insert(scheduleOnFromDate)
            schedulesService.insert(scheduleOnUntilDate)

            val attendance1 = anAttendance(scheduleUuid = scheduleOnFromDate.uuid, userUuid = testUserUuid, isAttending = null)
            val attendance2 = anAttendance(scheduleUuid = scheduleOnUntilDate.uuid, userUuid = testUser2Uuid, isAttending = null)
            attendancesService.insert(attendance1)
            attendancesService.insert(attendance2)

            val missing = attendancesForSlackbotService.getMissingAttendances(fromDate, untilDate)

            assertEquals(2, missing.size)
        }
    }
}
