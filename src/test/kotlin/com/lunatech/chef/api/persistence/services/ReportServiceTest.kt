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

class ReportServiceTest {
    private lateinit var reportService: ReportService
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
    private lateinit var testUserName: String
    private lateinit var testUser2Name: String

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()

        reportService = ReportService(database)
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
        testUserName = "Alice Smith"
        testUser2Name = "Bob Jones"
        val testUser = aUser(name = testUserName, emailAddress = uniqueEmail("alice"), officeUuid = testOfficeUuid)
        val testUser2 = aUser(name = testUser2Name, emailAddress = uniqueEmail("bob"), officeUuid = testOfficeUuid)
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
    }

    @Nested
    inner class GetReportByMonthOperations {
        @Test
        fun `getReportByMonth returns attendances for specified month`() {
            val today = LocalDate.now()
            val scheduleDate = today.withDayOfMonth(15)
            val schedule = aSchedule(menuUuid = testMenuUuid, date = scheduleDate, officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val attendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(attendance)

            val report = reportService.getReportByMonth(today.year, today.monthValue)

            assertEquals(1, report.size)
            val entry = report[0]
            assertEquals(scheduleDate, entry.date)
            assertEquals(testUserName, entry.name)
            assertEquals("Rotterdam", entry.city)
            assertEquals("Netherlands", entry.country)
        }

        @Test
        fun `getReportByMonth excludes non-attending users`() {
            val today = LocalDate.now()
            val scheduleDate = today.withDayOfMonth(10)
            val schedule = aSchedule(menuUuid = testMenuUuid, date = scheduleDate, officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val attendingAttendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUserUuid, isAttending = true)
            val notAttendingAttendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUser2Uuid, isAttending = false)
            attendancesService.insert(attendingAttendance)
            attendancesService.insert(notAttendingAttendance)

            val report = reportService.getReportByMonth(today.year, today.monthValue)

            assertEquals(1, report.size)
            assertEquals(testUserName, report[0].name)
        }

        @Test
        fun `getReportByMonth excludes deleted attendances`() {
            val today = LocalDate.now()
            val scheduleDate = today.withDayOfMonth(10)
            val schedule = aSchedule(menuUuid = testMenuUuid, date = scheduleDate, officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val activeAttendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUserUuid, isAttending = true)
            val deletedAttendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUser2Uuid, isAttending = true, isDeleted = true)
            attendancesService.insert(activeAttendance)
            attendancesService.insert(deletedAttendance)

            val report = reportService.getReportByMonth(today.year, today.monthValue)

            assertEquals(1, report.size)
            assertEquals(testUserName, report[0].name)
        }

        @Test
        fun `getReportByMonth excludes schedules outside the month`() {
            val today = LocalDate.now()
            val thisMonthDate = today.withDayOfMonth(10)
            val nextMonthDate = today.plusMonths(1).withDayOfMonth(10)

            val thisMonthSchedule = aSchedule(menuUuid = testMenuUuid, date = thisMonthDate, officeUuid = testOfficeUuid)
            val nextMonthSchedule = aSchedule(menuUuid = testMenuUuid, date = nextMonthDate, officeUuid = testOfficeUuid)
            schedulesService.insert(thisMonthSchedule)
            schedulesService.insert(nextMonthSchedule)

            val thisMonthAttendance = anAttendance(scheduleUuid = thisMonthSchedule.uuid, userUuid = testUserUuid, isAttending = true)
            val nextMonthAttendance = anAttendance(scheduleUuid = nextMonthSchedule.uuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(thisMonthAttendance)
            attendancesService.insert(nextMonthAttendance)

            val report = reportService.getReportByMonth(today.year, today.monthValue)

            assertEquals(1, report.size)
            assertEquals(thisMonthDate, report[0].date)
        }

        @Test
        fun `getReportByMonth returns multiple entries for multiple attendances`() {
            val today = LocalDate.now()
            val scheduleDate = today.withDayOfMonth(10)
            val schedule = aSchedule(menuUuid = testMenuUuid, date = scheduleDate, officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val attendance1 = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUserUuid, isAttending = true)
            val attendance2 = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUser2Uuid, isAttending = true)
            attendancesService.insert(attendance1)
            attendancesService.insert(attendance2)

            val report = reportService.getReportByMonth(today.year, today.monthValue)

            assertEquals(2, report.size)
            val userNames = report.map { it.name }
            assertTrue(userNames.contains(testUserName))
            assertTrue(userNames.contains(testUser2Name))
        }

        @Test
        fun `getReportByMonth returns entries ordered by date then name`() {
            val today = LocalDate.now()
            val date1 = today.withDayOfMonth(5)
            val date2 = today.withDayOfMonth(10)

            val schedule1 = aSchedule(menuUuid = testMenuUuid, date = date1, officeUuid = testOfficeUuid)
            val schedule2 = aSchedule(menuUuid = testMenuUuid, date = date2, officeUuid = testOfficeUuid)
            schedulesService.insert(schedule1)
            schedulesService.insert(schedule2)

            // Alice on date2, Bob on date1
            val attendance1 = anAttendance(scheduleUuid = schedule2.uuid, userUuid = testUserUuid, isAttending = true)
            val attendance2 = anAttendance(scheduleUuid = schedule1.uuid, userUuid = testUser2Uuid, isAttending = true)
            attendancesService.insert(attendance1)
            attendancesService.insert(attendance2)

            val report = reportService.getReportByMonth(today.year, today.monthValue)

            assertEquals(2, report.size)
            // First entry should be Bob on date1 (earlier date)
            assertEquals(date1, report[0].date)
            assertEquals(testUser2Name, report[0].name)
            // Second entry should be Alice on date2 (later date)
            assertEquals(date2, report[1].date)
            assertEquals(testUserName, report[1].name)
        }

        @Test
        fun `getReportByMonth returns empty list when no attendances`() {
            val today = LocalDate.now()

            val report = reportService.getReportByMonth(today.year, today.monthValue)

            assertTrue(report.isEmpty())
        }

        @Test
        fun `getReportByMonth handles different year and month`() {
            // Create a schedule for a specific month in the past
            val pastDate = LocalDate.of(2024, 6, 15)
            val schedule = aSchedule(menuUuid = testMenuUuid, date = pastDate, officeUuid = testOfficeUuid)
            schedulesService.insert(schedule)

            val attendance = anAttendance(scheduleUuid = schedule.uuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(attendance)

            val report = reportService.getReportByMonth(2024, 6)

            assertEquals(1, report.size)
            assertEquals(pastDate, report[0].date)
        }
    }
}
