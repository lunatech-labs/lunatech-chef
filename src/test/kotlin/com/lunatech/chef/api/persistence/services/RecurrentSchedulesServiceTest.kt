package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aRecurrentSchedule
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.routes.UpdatedRecurrentSchedule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class RecurrentSchedulesServiceTest {
    private lateinit var recurrentSchedulesService: RecurrentSchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testOffice2Uuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testMenu2Uuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        recurrentSchedulesService = RecurrentSchedulesService(database)
        menusService = MenusService(database)
        officesService = OfficesService(database)
        dishesService = DishesService(database)

        // Create test offices
        val testOffice = anOffice(city = "Rotterdam")
        val testOffice2 = anOffice(city = "Paris", country = "France")
        officesService.insert(testOffice)
        officesService.insert(testOffice2)
        testOfficeUuid = testOffice.uuid
        testOffice2Uuid = testOffice2.uuid

        // Create test dishes and menus
        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)
        val testMenu = aMenu(name = "Weekly Lunch", dishesUuids = listOf(testDish.uuid))
        val testMenu2 = aMenu(name = "Bi-weekly Dinner", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)
        menusService.insert(testMenu2)
        testMenuUuid = testMenu.uuid
        testMenu2Uuid = testMenu2.uuid
    }

    @Nested
    inner class InsertOperations {
        @Test
        fun `insert returns 1 when recurrent schedule is successfully created`() {
            val recurrentSchedule = aRecurrentSchedule(
                menuUuid = testMenuUuid,
                officeUuid = testOfficeUuid,
                repetitionDays = 7,
                nextDate = LocalDate.now().plusDays(7),
            )

            val insertResult = recurrentSchedulesService.insert(recurrentSchedule)

            assertEquals(1, insertResult, "Insert should return 1 for successful creation")
        }

        @Test
        fun `insert persists all recurrent schedule properties correctly`() {
            val nextDate = LocalDate.now().plusDays(7)
            val recurrentSchedule = aRecurrentSchedule(
                menuUuid = testMenuUuid,
                officeUuid = testOfficeUuid,
                repetitionDays = 7,
                nextDate = nextDate,
            )

            recurrentSchedulesService.insert(recurrentSchedule)
            val retrieved = recurrentSchedulesService.getByUuid(recurrentSchedule.uuid)

            assertEquals(1, retrieved.size)
            assertEquals(testMenuUuid, retrieved[0].menuUuid)
            assertEquals(testOfficeUuid, retrieved[0].officeUuid)
            assertEquals(7, retrieved[0].repetitionDays)
            assertEquals(nextDate, retrieved[0].nextDate)
        }

        @Test
        fun `insert with different repetition days persists correctly`() {
            val schedule14Days = aRecurrentSchedule(
                menuUuid = testMenuUuid,
                officeUuid = testOfficeUuid,
                repetitionDays = 14,
            )

            recurrentSchedulesService.insert(schedule14Days)
            val retrieved = recurrentSchedulesService.getByUuid(schedule14Days.uuid)

            assertEquals(14, retrieved[0].repetitionDays)
        }
    }

    @Nested
    inner class ReadOperations {
        @Test
        fun `getAll returns only non-deleted recurrent schedules`() {
            val schedule1 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
            val schedule2 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 14)
            val deletedSchedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, isDeleted = true)

            recurrentSchedulesService.insert(schedule1)
            recurrentSchedulesService.insert(schedule2)
            recurrentSchedulesService.insert(deletedSchedule)

            val allSchedules = recurrentSchedulesService.getAll()

            assertEquals(2, allSchedules.size, "Should return only non-deleted schedules")
            assertTrue(allSchedules.none { it.uuid == deletedSchedule.uuid }, "Deleted schedule should not be in results")
        }

        @Test
        fun `getAll returns empty list when no schedules exist`() {
            val allSchedules = recurrentSchedulesService.getAll()

            assertTrue(allSchedules.isEmpty(), "Should return empty list when no schedules exist")
        }

        @Test
        fun `getByUuid returns schedule when it exists`() {
            val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid)
            recurrentSchedulesService.insert(schedule)

            val retrieved = recurrentSchedulesService.getByUuid(schedule.uuid)

            assertEquals(1, retrieved.size, "Should return exactly one schedule")
            assertEquals(schedule.uuid, retrieved[0].uuid)
        }

        @Test
        fun `getByUuid returns empty list for non-existent UUID`() {
            val nonExistentUuid = UUID.randomUUID()

            val result = recurrentSchedulesService.getByUuid(nonExistentUuid)

            assertTrue(result.isEmpty(), "Should return empty list for non-existent UUID")
        }

        @Test
        fun `getByUuid returns deleted schedule without filtering`() {
            val deletedSchedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, isDeleted = true)
            recurrentSchedulesService.insert(deletedSchedule)

            val retrieved = recurrentSchedulesService.getByUuid(deletedSchedule.uuid)

            assertEquals(1, retrieved.size, "getByUuid should return deleted schedules")
            assertTrue(retrieved[0].isDeleted)
        }

        @Test
        fun `getIntervalDate returns schedules within date range`() {
            val baseDate = LocalDate.now()
            val scheduleInRange1 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = baseDate.plusDays(5))
            val scheduleInRange2 = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = baseDate.plusDays(10))
            val scheduleOutOfRange = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = baseDate.plusDays(20))

            recurrentSchedulesService.insert(scheduleInRange1)
            recurrentSchedulesService.insert(scheduleInRange2)
            recurrentSchedulesService.insert(scheduleOutOfRange)

            val schedulesInRange = recurrentSchedulesService.getIntervalDate(baseDate, baseDate.plusDays(15))

            assertEquals(2, schedulesInRange.size)
            assertTrue(schedulesInRange.any { it.uuid == scheduleInRange1.uuid })
            assertTrue(schedulesInRange.any { it.uuid == scheduleInRange2.uuid })
        }

        @Test
        fun `getIntervalDate excludes deleted schedules`() {
            val baseDate = LocalDate.now()
            val activeSchedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = baseDate.plusDays(5))
            val deletedSchedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = baseDate.plusDays(6), isDeleted = true)

            recurrentSchedulesService.insert(activeSchedule)
            recurrentSchedulesService.insert(deletedSchedule)

            val schedulesInRange = recurrentSchedulesService.getIntervalDate(baseDate, baseDate.plusDays(10))

            assertEquals(1, schedulesInRange.size)
            assertEquals(activeSchedule.uuid, schedulesInRange[0].uuid)
        }
    }

    @Nested
    inner class UpdateOperations {
        @Test
        fun `update returns 1 when schedule is successfully updated`() {
            val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
            recurrentSchedulesService.insert(schedule)

            val updatedSchedule = UpdatedRecurrentSchedule(
                menuUuid = testMenu2Uuid,
                officeUuid = testOffice2Uuid,
                repetitionDays = 14,
                nextDate = LocalDate.now().plusDays(14),
            )

            val updateResult = recurrentSchedulesService.update(schedule.uuid, updatedSchedule)

            assertEquals(1, updateResult, "Update should return 1 for successful update")
        }

        @Test
        fun `update modifies all schedule properties`() {
            val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, repetitionDays = 7)
            recurrentSchedulesService.insert(schedule)

            val newNextDate = LocalDate.now().plusDays(14)
            val updatedSchedule = UpdatedRecurrentSchedule(
                menuUuid = testMenu2Uuid,
                officeUuid = testOffice2Uuid,
                repetitionDays = 14,
                nextDate = newNextDate,
            )
            recurrentSchedulesService.update(schedule.uuid, updatedSchedule)

            val retrieved = recurrentSchedulesService.getByUuid(schedule.uuid)[0]

            assertEquals(testMenu2Uuid, retrieved.menuUuid)
            assertEquals(testOffice2Uuid, retrieved.officeUuid)
            assertEquals(14, retrieved.repetitionDays)
            assertEquals(newNextDate, retrieved.nextDate)
        }

        @Test
        fun `update returns 0 for non-existent schedule`() {
            val nonExistentUuid = UUID.randomUUID()
            val updatedSchedule = UpdatedRecurrentSchedule(
                menuUuid = testMenuUuid,
                officeUuid = testOfficeUuid,
                repetitionDays = 7,
                nextDate = LocalDate.now().plusDays(7),
            )

            val updateResult = recurrentSchedulesService.update(nonExistentUuid, updatedSchedule)

            assertEquals(0, updateResult, "Update should return 0 for non-existent schedule")
        }
    }

    @Nested
    inner class DeleteOperations {
        @Test
        fun `delete performs soft delete by setting isDeleted flag`() {
            val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid)
            recurrentSchedulesService.insert(schedule)

            recurrentSchedulesService.delete(schedule.uuid)

            val retrieved = recurrentSchedulesService.getByUuid(schedule.uuid)
            assertEquals(1, retrieved.size, "Schedule should still exist in database")
            assertTrue(retrieved[0].isDeleted, "isDeleted flag should be set to true")
        }

        @Test
        fun `delete removes schedule from getAll results`() {
            val schedule = aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid)
            recurrentSchedulesService.insert(schedule)

            recurrentSchedulesService.delete(schedule.uuid)

            val allSchedules = recurrentSchedulesService.getAll()
            assertTrue(allSchedules.isEmpty(), "Deleted schedule should not appear in getAll")
        }

        @Test
        fun `delete returns 0 for non-existent schedule`() {
            val nonExistentUuid = UUID.randomUUID()

            val deleteResult = recurrentSchedulesService.delete(nonExistentUuid)

            assertEquals(0, deleteResult, "Delete should return 0 for non-existent schedule")
        }
    }
}
