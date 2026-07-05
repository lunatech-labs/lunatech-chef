package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.anExternalAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class ExternalAttendancesWithScheduleInfoServiceTest {
    private lateinit var externalAttendancesWithScheduleInfoService: ExternalAttendancesWithScheduleInfoService
    private lateinit var externalAttendancesService: ExternalAttendancesService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var menusWithDishesNamesService: MenusWithDishesNamesService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testOffice2Uuid: UUID
    private lateinit var testMenuUuid: UUID
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
        menusWithDishesNamesService = MenusWithDishesNamesService(database)
        externalAttendancesService = ExternalAttendancesService(database)
        externalAttendancesWithScheduleInfoService =
            ExternalAttendancesWithScheduleInfoService(database, schedulesService, menusWithDishesNamesService)

        // Create test offices
        val testOffice = anOffice(city = "Rotterdam")
        val testOffice2 = anOffice(city = "Paris", country = "France")
        officesService.insert(testOffice)
        officesService.insert(testOffice2)
        testOfficeUuid = testOffice.uuid
        testOffice2Uuid = testOffice2.uuid

        // Create test dish and menu
        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)
        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid

        // Create test schedules
        val testSchedule =
            aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
        val testSchedule2 =
            aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(14), officeUuid = testOffice2Uuid)
        schedulesService.insert(testSchedule)
        schedulesService.insert(testSchedule2)
        testScheduleUuid = testSchedule.uuid
        testSchedule2Uuid = testSchedule2.uuid
    }

    @Nested
    inner class GetAllFromDateAndOfficeOperations {
        @Test
        fun `getAllFromDateAndOffice returns external attendances with schedule info`() {
            val externalAttendance =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 5)
            externalAttendancesService.insert(externalAttendance)

            val externalAttendances = externalAttendancesWithScheduleInfoService.getAllFromDateAndOffice(null, null)

            assertEquals(1, externalAttendances.size)
            val retrieved = externalAttendances[0]
            assertEquals(externalAttendance.uuid, retrieved.uuid)
            assertEquals(testScheduleUuid, retrieved.scheduleUuid)
            assertEquals("Lunch Menu", retrieved.menu.name)
            assertEquals("Rotterdam", retrieved.office)
            assertEquals(5, retrieved.attendancesCount)
        }

        @Test
        fun `getAllFromDateAndOffice filters by fromDate`() {
            val pastSchedule =
                aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(5), officeUuid = testOfficeUuid)
            schedulesService.insert(pastSchedule)

            val pastExternalAttendance =
                anExternalAttendance(scheduleUuid = pastSchedule.uuid, attendancesCount = 2)
            val futureExternalAttendance =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 3)
            externalAttendancesService.insert(pastExternalAttendance)
            externalAttendancesService.insert(futureExternalAttendance)

            val externalAttendances =
                externalAttendancesWithScheduleInfoService.getAllFromDateAndOffice(LocalDate.now(), null)

            assertEquals(1, externalAttendances.size)
            assertEquals(testScheduleUuid, externalAttendances[0].scheduleUuid)
        }

        @Test
        fun `getAllFromDateAndOffice filters by office`() {
            val externalAttendance1 =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 3)
            val externalAttendance2 =
                anExternalAttendance(scheduleUuid = testSchedule2Uuid, attendancesCount = 4)
            externalAttendancesService.insert(externalAttendance1)
            externalAttendancesService.insert(externalAttendance2)

            val externalAttendances =
                externalAttendancesWithScheduleInfoService.getAllFromDateAndOffice(null, testOfficeUuid)

            assertEquals(1, externalAttendances.size)
            assertEquals("Rotterdam", externalAttendances[0].office)
        }

        @Test
        fun `getAllFromDateAndOffice filters by both fromDate and office`() {
            val pastSchedule =
                aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(5), officeUuid = testOfficeUuid)
            schedulesService.insert(pastSchedule)

            val pastExternalAttendance =
                anExternalAttendance(scheduleUuid = pastSchedule.uuid, attendancesCount = 1)
            val futureExternalAttendance1 =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 2)
            val futureExternalAttendance2 =
                anExternalAttendance(scheduleUuid = testSchedule2Uuid, attendancesCount = 3)
            externalAttendancesService.insert(pastExternalAttendance)
            externalAttendancesService.insert(futureExternalAttendance1)
            externalAttendancesService.insert(futureExternalAttendance2)

            val externalAttendances =
                externalAttendancesWithScheduleInfoService.getAllFromDateAndOffice(LocalDate.now(), testOfficeUuid)

            assertEquals(1, externalAttendances.size)
            assertEquals(testScheduleUuid, externalAttendances[0].scheduleUuid)
            assertEquals("Rotterdam", externalAttendances[0].office)
        }

        @Test
        fun `getAllFromDateAndOffice excludes deleted external attendances`() {
            val activeExternalAttendance =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 5)
            val deletedExternalAttendance =
                anExternalAttendance(scheduleUuid = testSchedule2Uuid, attendancesCount = 3, isDeleted = true)
            externalAttendancesService.insert(activeExternalAttendance)
            externalAttendancesService.insert(deletedExternalAttendance)

            val externalAttendances = externalAttendancesWithScheduleInfoService.getAllFromDateAndOffice(null, null)

            assertEquals(1, externalAttendances.size)
            assertEquals(activeExternalAttendance.uuid, externalAttendances[0].uuid)
        }

        @Test
        fun `getAllFromDateAndOffice returns empty list when no external attendances`() {
            val externalAttendances = externalAttendancesWithScheduleInfoService.getAllFromDateAndOffice(null, null)

            assertTrue(externalAttendances.isEmpty())
        }

        @Test
        fun `getAllFromDateAndOffice returns external attendances ordered by date`() {
            val externalAttendance1 =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 5)
            val externalAttendance2 =
                anExternalAttendance(scheduleUuid = testSchedule2Uuid, attendancesCount = 3)
            externalAttendancesService.insert(externalAttendance1)
            externalAttendancesService.insert(externalAttendance2)

            val externalAttendances = externalAttendancesWithScheduleInfoService.getAllFromDateAndOffice(null, null)

            assertEquals(2, externalAttendances.size)
            // testSchedule is 7 days from now, testSchedule2 is 14 days from now
            assertEquals(testScheduleUuid, externalAttendances[0].scheduleUuid)
            assertEquals(testSchedule2Uuid, externalAttendances[1].scheduleUuid)
        }
    }
}
