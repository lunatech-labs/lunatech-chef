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

class SchedulesWithAttendanceInfoServiceTest {
    private lateinit var schedulesWithAttendanceInfoService: SchedulesWithAttendanceInfoService
    private lateinit var attendancesService: AttendancesService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var usersService: UsersService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testOffice2Uuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testUserUuid: UUID
    private lateinit var testUser2Uuid: UUID
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
        schedulesWithAttendanceInfoService = SchedulesWithAttendanceInfoService(database, menusService)

        // Create test offices
        val testOffice = anOffice(city = "Rotterdam")
        val testOffice2 = anOffice(city = "Paris", country = "France")
        officesService.insert(testOffice)
        officesService.insert(testOffice2)
        testOfficeUuid = testOffice.uuid
        testOffice2Uuid = testOffice2.uuid

        // Create test users
        val testUser = aUser(name = "Alice Smith", emailAddress = uniqueEmail("alice"), officeUuid = testOfficeUuid, isVegetarian = true)
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
        val testSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
        schedulesService.insert(testSchedule)
        testScheduleUuid = testSchedule.uuid
    }

    @Nested
    inner class GetFilteredOperations {
        @Test
        fun `getFiltered returns schedules with attendance info`() {
            val attendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(attendance)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(null, null)

            assertEquals(1, schedules.size)
            val retrieved = schedules[0]
            assertEquals(testScheduleUuid, retrieved.uuid)
            assertEquals("Lunch Menu", retrieved.menuName)
            assertEquals("Rotterdam", retrieved.office)
            assertEquals(1, retrieved.attendants.size)
            assertEquals("Alice Smith", retrieved.attendants[0].name)
        }

        @Test
        fun `getFiltered returns only attending users`() {
            val attendingUser = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            val notAttendingUser = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUser2Uuid, isAttending = false)
            attendancesService.insert(attendingUser)
            attendancesService.insert(notAttendingUser)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(null, null)

            assertEquals(1, schedules.size)
            assertEquals(1, schedules[0].attendants.size)
            assertEquals("Alice Smith", schedules[0].attendants[0].name)
        }

        @Test
        fun `getFiltered excludes deleted attendances`() {
            val activeAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            val deletedAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUser2Uuid, isAttending = true, isDeleted = true)
            attendancesService.insert(activeAttendance)
            attendancesService.insert(deletedAttendance)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(null, null)

            assertEquals(1, schedules.size)
            assertEquals(1, schedules[0].attendants.size)
            assertEquals("Alice Smith", schedules[0].attendants[0].name)
        }

        @Test
        fun `getFiltered filters by fromDate`() {
            val pastSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(5), officeUuid = testOfficeUuid)
            schedulesService.insert(pastSchedule)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(LocalDate.now(), null)

            assertEquals(1, schedules.size)
            assertEquals(testScheduleUuid, schedules[0].uuid)
        }

        @Test
        fun `getFiltered filters by office`() {
            val schedule2 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(8), officeUuid = testOffice2Uuid)
            schedulesService.insert(schedule2)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(null, testOfficeUuid)

            assertEquals(1, schedules.size)
            assertEquals("Rotterdam", schedules[0].office)
        }

        @Test
        fun `getFiltered filters by both fromDate and office`() {
            val pastSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(5), officeUuid = testOfficeUuid)
            val futureSchedule2 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(8), officeUuid = testOffice2Uuid)
            schedulesService.insert(pastSchedule)
            schedulesService.insert(futureSchedule2)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(LocalDate.now(), testOfficeUuid)

            assertEquals(1, schedules.size)
            assertEquals(testScheduleUuid, schedules[0].uuid)
        }

        @Test
        fun `getFiltered excludes deleted schedules`() {
            val deletedSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(10), officeUuid = testOfficeUuid, isDeleted = true)
            schedulesService.insert(deletedSchedule)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(null, null)

            assertEquals(1, schedules.size)
            assertEquals(testScheduleUuid, schedules[0].uuid)
        }

        @Test
        fun `getFiltered returns schedules ordered by date`() {
            val laterSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(14), officeUuid = testOfficeUuid)
            schedulesService.insert(laterSchedule)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(null, null)

            assertEquals(2, schedules.size)
            assertEquals(testScheduleUuid, schedules[0].uuid) // 7 days from now
            assertEquals(laterSchedule.uuid, schedules[1].uuid) // 14 days from now
        }

        @Test
        fun `getFiltered returns attendants ordered by name`() {
            val attendance1 = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true) // Alice Smith
            val attendance2 = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUser2Uuid, isAttending = true) // Bob Jones
            attendancesService.insert(attendance1)
            attendancesService.insert(attendance2)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(null, null)

            assertEquals(2, schedules[0].attendants.size)
            assertEquals("Alice Smith", schedules[0].attendants[0].name)
            assertEquals("Bob Jones", schedules[0].attendants[1].name)
        }

        @Test
        fun `getFiltered returns empty attendants list when no one attending`() {
            val schedules = schedulesWithAttendanceInfoService.getFiltered(null, null)

            assertEquals(1, schedules.size)
            assertTrue(schedules[0].attendants.isEmpty())
        }

        @Test
        fun `getFiltered returns empty list when no schedules`() {
            // Delete the test schedule created in setup
            schedulesService.delete(testScheduleUuid)

            val schedules = schedulesWithAttendanceInfoService.getFiltered(null, null)

            assertTrue(schedules.isEmpty())
        }
    }
}
