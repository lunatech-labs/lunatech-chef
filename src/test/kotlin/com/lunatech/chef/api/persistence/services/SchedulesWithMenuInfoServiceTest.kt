package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class SchedulesWithMenuInfoServiceTest {
    private lateinit var schedulesWithMenuInfoService: SchedulesWithMenuInfoService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var menusWithDishesNamesService: MenusWithDishesNamesService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testOffice2Uuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testDishName: String

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        dishesService = DishesService(database)
        officesService = OfficesService(database)
        menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        menusWithDishesNamesService = MenusWithDishesNamesService(database)
        schedulesWithMenuInfoService = SchedulesWithMenuInfoService(database, menusWithDishesNamesService)

        // Create test offices
        val testOffice = anOffice(city = "Rotterdam")
        val testOffice2 = anOffice(city = "Paris", country = "France")
        officesService.insert(testOffice)
        officesService.insert(testOffice2)
        testOfficeUuid = testOffice.uuid
        testOffice2Uuid = testOffice2.uuid

        // Create test dish and menu
        testDishName = "Pasta"
        val testDish = aDish(name = testDishName, isVegetarian = true)
        dishesService.insert(testDish)
        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid
    }

    @Nested
    inner class GetAllOperations {
        @Test
        fun `getAll returns schedules with menu and office info`() {
            val schedule = aSchedule(
                menuUuid = testMenuUuid,
                date = LocalDate.now().plusDays(7),
                officeUuid = testOfficeUuid,
            )
            schedulesService.insert(schedule)

            val allSchedules = schedulesWithMenuInfoService.getAll()

            assertEquals(1, allSchedules.size)
            val retrieved = allSchedules[0]
            assertEquals(schedule.uuid, retrieved.uuid)
            assertEquals(schedule.date, retrieved.date)
            assertEquals("Lunch Menu", retrieved.menu.name)
            assertEquals("Rotterdam", retrieved.office.city)
            assertEquals(1, retrieved.menu.dishes.size)
            assertEquals(testDishName, retrieved.menu.dishes[0].name)
        }

        @Test
        fun `getAll returns only non-deleted schedules ordered by date`() {
            val schedule1 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(10), officeUuid = testOfficeUuid)
            val schedule2 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(5), officeUuid = testOfficeUuid)
            val deletedSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(3), officeUuid = testOfficeUuid, isDeleted = true)

            schedulesService.insert(schedule1)
            schedulesService.insert(schedule2)
            schedulesService.insert(deletedSchedule)

            val allSchedules = schedulesWithMenuInfoService.getAll()

            assertEquals(2, allSchedules.size)
            assertTrue(allSchedules.none { it.uuid == deletedSchedule.uuid })
            // Check ordering by date ascending
            assertEquals(schedule2.uuid, allSchedules[0].uuid)
            assertEquals(schedule1.uuid, allSchedules[1].uuid)
        }

        @Test
        fun `getAll returns empty list when no schedules exist`() {
            val allSchedules = schedulesWithMenuInfoService.getAll()

            assertTrue(allSchedules.isEmpty())
        }
    }

    @Nested
    inner class GetFilteredOperations {
        @Test
        fun `getFiltered by fromDate returns schedules on or after date`() {
            val pastSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(5), officeUuid = testOfficeUuid)
            val futureSchedule = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(5), officeUuid = testOfficeUuid)

            schedulesService.insert(pastSchedule)
            schedulesService.insert(futureSchedule)

            val filteredSchedules = schedulesWithMenuInfoService.getFiltered(LocalDate.now(), null)

            assertEquals(1, filteredSchedules.size)
            assertEquals(futureSchedule.uuid, filteredSchedules[0].uuid)
        }

        @Test
        fun `getFiltered by office returns schedules for specific office`() {
            val schedule1 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            val schedule2 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(8), officeUuid = testOffice2Uuid)

            schedulesService.insert(schedule1)
            schedulesService.insert(schedule2)

            val filteredSchedules = schedulesWithMenuInfoService.getFiltered(null, testOfficeUuid)

            assertEquals(1, filteredSchedules.size)
            assertEquals("Rotterdam", filteredSchedules[0].office.city)
        }

        @Test
        fun `getFiltered by both fromDate and office`() {
            val schedule1 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOfficeUuid)
            val schedule2 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().minusDays(1), officeUuid = testOfficeUuid)
            val schedule3 = aSchedule(menuUuid = testMenuUuid, date = LocalDate.now().plusDays(7), officeUuid = testOffice2Uuid)

            schedulesService.insert(schedule1)
            schedulesService.insert(schedule2)
            schedulesService.insert(schedule3)

            val filteredSchedules = schedulesWithMenuInfoService.getFiltered(LocalDate.now(), testOfficeUuid)

            assertEquals(1, filteredSchedules.size)
            assertEquals(schedule1.uuid, filteredSchedules[0].uuid)
        }
    }

    @Nested
    inner class GetByUuidOperations {
        @Test
        fun `getByUuid returns schedule with menu and office info`() {
            val schedule = aSchedule(
                menuUuid = testMenuUuid,
                date = LocalDate.now().plusDays(7),
                officeUuid = testOfficeUuid,
            )
            schedulesService.insert(schedule)

            val retrieved = schedulesWithMenuInfoService.getByUuid(schedule.uuid)

            assertEquals(1, retrieved.size)
            assertEquals(schedule.uuid, retrieved[0].uuid)
            assertEquals("Lunch Menu", retrieved[0].menu.name)
            assertEquals("Rotterdam", retrieved[0].office.city)
        }

        @Test
        fun `getByUuid returns empty list for non-existent schedule`() {
            val result = schedulesWithMenuInfoService.getByUuid(UUID.randomUUID())

            assertTrue(result.isEmpty())
        }
    }
}
