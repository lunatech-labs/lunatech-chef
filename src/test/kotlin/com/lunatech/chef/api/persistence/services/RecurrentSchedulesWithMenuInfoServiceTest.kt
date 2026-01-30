package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aRecurrentSchedule
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class RecurrentSchedulesWithMenuInfoServiceTest {
    private lateinit var recurrentSchedulesWithMenuInfoService: RecurrentSchedulesWithMenuInfoService
    private lateinit var recurrentSchedulesService: RecurrentSchedulesService
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
        menusWithDishesNamesService = MenusWithDishesNamesService(database)
        recurrentSchedulesService = RecurrentSchedulesService(database)
        recurrentSchedulesWithMenuInfoService = RecurrentSchedulesWithMenuInfoService(database, menusWithDishesNamesService)

        // Create test offices
        val testOffice = anOffice(city = "Rotterdam")
        val testOffice2 = anOffice(city = "Paris", country = "France")
        officesService.insert(testOffice)
        officesService.insert(testOffice2)
        testOfficeUuid = testOffice.uuid
        testOffice2Uuid = testOffice2.uuid

        // Create test dish and menu
        testDishName = "Pasta"
        val testDish = aDish(name = testDishName, description = "Italian pasta", isVegetarian = true)
        dishesService.insert(testDish)
        val testMenu = aMenu(name = "Weekly Lunch", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        testMenuUuid = testMenu.uuid
    }

    @Nested
    inner class GetAllOperations {
        @Test
        fun `getAll returns recurrent schedules with menu and office info`() {
            val recurrentSchedule = aRecurrentSchedule(
                menuUuid = testMenuUuid,
                officeUuid = testOfficeUuid,
                repetitionDays = 7,
                nextDate = LocalDate.now().plusDays(7),
            )
            recurrentSchedulesService.insert(recurrentSchedule)

            val allSchedules = recurrentSchedulesWithMenuInfoService.getAll()

            assertEquals(1, allSchedules.size)
            val retrieved = allSchedules[0]
            assertEquals(recurrentSchedule.uuid, retrieved.uuid)
            assertEquals(recurrentSchedule.nextDate, retrieved.nextDate)
            assertEquals(7, retrieved.repetitionDays)
            assertEquals("Weekly Lunch", retrieved.menu.name)
            assertEquals("Rotterdam", retrieved.office.city)
            assertEquals(1, retrieved.menu.dishes.size)
            assertEquals(testDishName, retrieved.menu.dishes[0].name)
        }

        @Test
        fun `getAll returns only non-deleted recurrent schedules`() {
            val schedule1 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
            val deletedSchedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 14, isDeleted = true)

            recurrentSchedulesService.insert(schedule1)
            recurrentSchedulesService.insert(deletedSchedule)

            val allSchedules = recurrentSchedulesWithMenuInfoService.getAll()

            assertEquals(1, allSchedules.size)
            assertEquals(schedule1.uuid, allSchedules[0].uuid)
        }

        @Test
        fun `getAll returns empty list when no recurrent schedules exist`() {
            val allSchedules = recurrentSchedulesWithMenuInfoService.getAll()

            assertTrue(allSchedules.isEmpty())
        }
    }

    @Nested
    inner class GetFilteredOperations {
        @Test
        fun `getFiltered by office returns recurrent schedules for specific office`() {
            val schedule1 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
            val schedule2 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOffice2Uuid, repetitionDays = 7)

            recurrentSchedulesService.insert(schedule1)
            recurrentSchedulesService.insert(schedule2)

            val filteredSchedules = recurrentSchedulesWithMenuInfoService.getFiltered(testOfficeUuid)

            assertEquals(1, filteredSchedules.size)
            assertEquals("Rotterdam", filteredSchedules[0].office.city)
        }

        @Test
        fun `getFiltered with null office returns all non-deleted schedules`() {
            val schedule1 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
            val schedule2 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOffice2Uuid, repetitionDays = 7)

            recurrentSchedulesService.insert(schedule1)
            recurrentSchedulesService.insert(schedule2)

            val allSchedules = recurrentSchedulesWithMenuInfoService.getFiltered(null)

            assertEquals(2, allSchedules.size)
        }

        @Test
        fun `getFiltered excludes deleted recurrent schedules`() {
            val activeSchedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
            val deletedSchedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 14, isDeleted = true)

            recurrentSchedulesService.insert(activeSchedule)
            recurrentSchedulesService.insert(deletedSchedule)

            val filteredSchedules = recurrentSchedulesWithMenuInfoService.getFiltered(testOfficeUuid)

            assertEquals(1, filteredSchedules.size)
            assertEquals(activeSchedule.uuid, filteredSchedules[0].uuid)
        }
    }

    @Nested
    inner class GetByUuidOperations {
        @Test
        fun `getByUuid returns recurrent schedule with menu and office info`() {
            val recurrentSchedule = aRecurrentSchedule(
                menuUuid = testMenuUuid,
                officeUuid = testOfficeUuid,
                repetitionDays = 14,
                nextDate = LocalDate.now().plusDays(14),
            )
            recurrentSchedulesService.insert(recurrentSchedule)

            val retrieved = recurrentSchedulesWithMenuInfoService.getByUuid(recurrentSchedule.uuid)

            assertEquals(1, retrieved.size)
            assertEquals(recurrentSchedule.uuid, retrieved[0].uuid)
            assertEquals(14, retrieved[0].repetitionDays)
            assertEquals("Weekly Lunch", retrieved[0].menu.name)
            assertEquals("Rotterdam", retrieved[0].office.city)
        }

        @Test
        fun `getByUuid returns empty list for non-existent schedule`() {
            val result = recurrentSchedulesWithMenuInfoService.getByUuid(UUID.randomUUID())

            assertTrue(result.isEmpty())
        }

        @Test
        fun `getByUuid returns menu dishes with all properties`() {
            val recurrentSchedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
            recurrentSchedulesService.insert(recurrentSchedule)

            val retrieved = recurrentSchedulesWithMenuInfoService.getByUuid(recurrentSchedule.uuid)[0]
            val dish = retrieved.menu.dishes[0]

            assertEquals(testDishName, dish.name)
            assertEquals("Italian pasta", dish.description)
            assertTrue(dish.isVegetarian)
        }
    }
}
