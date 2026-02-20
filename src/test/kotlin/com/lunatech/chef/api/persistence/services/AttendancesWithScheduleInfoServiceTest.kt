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

class AttendancesWithScheduleInfoServiceTest {
    private lateinit var attendancesWithScheduleInfoService: AttendancesWithScheduleInfoService
    private lateinit var attendancesService: AttendancesService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var menusWithDishesNamesService: MenusWithDishesNamesService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService
    private lateinit var usersService: UsersService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testOffice2Uuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testUserUuid: UUID
    private lateinit var testScheduleUuid: UUID
    private lateinit var testSchedule2Uuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()

        officesService = OfficesService(database)
        dishesService = DishesService(database)
        menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        usersService = UsersService(database)
        menusWithDishesNamesService = MenusWithDishesNamesService(database)
        attendancesService = AttendancesService(database, usersService, schedulesService)
        attendancesWithScheduleInfoService = AttendancesWithScheduleInfoService(database, schedulesService, menusWithDishesNamesService)

        // Create test offices
        val testOffice = anOffice(city = "Rotterdam")
        val testOffice2 = anOffice(city = "Paris", country = "France")
        officesService.insert(testOffice)
        officesService.insert(testOffice2)
        testOfficeUuid = testOffice.uuid
        testOffice2Uuid = testOffice2.uuid

        // Create test user
        val testUser = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
        usersService.insert(testUser)
        testUserUuid = testUser.uuid

        // Create test dish and menu
        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)
        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid

        // Create test schedules
        val testSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
        val testSchedule2 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(14), officeUuid = testOffice2Uuid)
        schedulesService.insert(testSchedule)
        schedulesService.insert(testSchedule2)
        testScheduleUuid = testSchedule.uuid
        testSchedule2Uuid = testSchedule2.uuid
    }

    @Nested
    inner class GetByUserUuidFilteredOperations {
        @Test
        fun `getByUserUuidFiltered returns attendances with schedule info`() {
            val attendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(attendance)

            val attendances = attendancesWithScheduleInfoService.getByUserUuidFiltered(testUserUuid, null, null)

            assertEquals(1, attendances.size)
            val retrieved = attendances[0]
            assertEquals(attendance.uuid, retrieved.uuid)
            assertEquals(testUserUuid, retrieved.userUuid)
            assertEquals(testScheduleUuid, retrieved.scheduleUuid)
            assertEquals("Lunch Menu", retrieved.menu.name)
            assertEquals("Rotterdam", retrieved.office)
            assertTrue(retrieved.isAttending)
        }

        @Test
        fun `getByUserUuidFiltered filters by fromDate`() {
            val pastSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(5), officeUuid = testOfficeUuid)
            schedulesService.insert(pastSchedule)

            val pastAttendance = anAttendance(scheduleUuid = pastSchedule.uuid, userUuid = testUserUuid, isAttending = true)
            val futureAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(pastAttendance)
            attendancesService.insert(futureAttendance)

            val attendances = attendancesWithScheduleInfoService.getByUserUuidFiltered(testUserUuid, LocalDate.now(), null)

            assertEquals(1, attendances.size)
            assertEquals(testScheduleUuid, attendances[0].scheduleUuid)
        }

        @Test
        fun `getByUserUuidFiltered filters by office`() {
            val attendance1 = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            val attendance2 = anAttendance(scheduleUuid = testSchedule2Uuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(attendance1)
            attendancesService.insert(attendance2)

            val attendances = attendancesWithScheduleInfoService.getByUserUuidFiltered(testUserUuid, null, testOfficeUuid)

            assertEquals(1, attendances.size)
            assertEquals("Rotterdam", attendances[0].office)
        }

        @Test
        fun `getByUserUuidFiltered filters by both fromDate and office`() {
            val pastSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(5), officeUuid = testOfficeUuid)
            schedulesService.insert(pastSchedule)

            val pastAttendance = anAttendance(scheduleUuid = pastSchedule.uuid, userUuid = testUserUuid, isAttending = true)
            val futureAttendance1 = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            val futureAttendance2 = anAttendance(scheduleUuid = testSchedule2Uuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(pastAttendance)
            attendancesService.insert(futureAttendance1)
            attendancesService.insert(futureAttendance2)

            val attendances = attendancesWithScheduleInfoService.getByUserUuidFiltered(testUserUuid, LocalDate.now(), testOfficeUuid)

            assertEquals(1, attendances.size)
            assertEquals(testScheduleUuid, attendances[0].scheduleUuid)
            assertEquals("Rotterdam", attendances[0].office)
        }

        @Test
        fun `getByUserUuidFiltered excludes deleted attendances`() {
            val activeAttendance = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            val deletedAttendance =
                anAttendance(scheduleUuid = testSchedule2Uuid, userUuid = testUserUuid, isAttending = true, isDeleted = true)
            attendancesService.insert(activeAttendance)
            attendancesService.insert(deletedAttendance)

            val attendances = attendancesWithScheduleInfoService.getByUserUuidFiltered(testUserUuid, null, null)

            assertEquals(1, attendances.size)
            assertEquals(activeAttendance.uuid, attendances[0].uuid)
        }

        @Test
        fun `getByUserUuidFiltered returns empty list when no attendances`() {
            val attendances = attendancesWithScheduleInfoService.getByUserUuidFiltered(testUserUuid, null, null)

            assertTrue(attendances.isEmpty())
        }

        @Test
        fun `getByUserUuidFiltered returns attendances ordered by date`() {
            val attendance1 = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            val attendance2 = anAttendance(scheduleUuid = testSchedule2Uuid, userUuid = testUserUuid, isAttending = true)
            attendancesService.insert(attendance1)
            attendancesService.insert(attendance2)

            val attendances = attendancesWithScheduleInfoService.getByUserUuidFiltered(testUserUuid, null, null)

            assertEquals(2, attendances.size)
            // testSchedule is 7 days from now, testSchedule2 is 14 days from now
            assertEquals(testScheduleUuid, attendances[0].scheduleUuid)
            assertEquals(testSchedule2Uuid, attendances[1].scheduleUuid)
        }
    }
}
