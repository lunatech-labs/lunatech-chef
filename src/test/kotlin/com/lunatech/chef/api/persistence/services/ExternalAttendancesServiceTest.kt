package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.ExternalAttendance
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.anExternalAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.schemas.ExternalAttendances
import com.lunatech.chef.api.routes.UpdatedExternalAttendance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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

class ExternalAttendancesServiceTest {
    private lateinit var externalAttendancesService: ExternalAttendancesService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testScheduleUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()

        officesService = OfficesService(database)
        dishesService = DishesService(database)
        menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        externalAttendancesService = ExternalAttendancesService(database)

        // Create test office
        val testOffice = anOffice(city = "Rotterdam")
        officesService.insert(testOffice)
        testOfficeUuid = testOffice.uuid

        // Create test dish and menu
        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)
        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)

        // Create test schedule
        val testSchedule =
            aSchedule(
                menuUuid = testMenu.uuid,
                date = LocalDate.now().plusDays(7),
                officeUuid = testOfficeUuid,
            )
        schedulesService.insert(testSchedule)
        testScheduleUuid = testSchedule.uuid
    }

    // Helper to retrieve external attendance directly from database for testing
    private fun getExternalAttendanceByUuid(uuid: UUID): ExternalAttendance? {
        val database = TestDatabase.getDatabase()
        return database
            .from(ExternalAttendances)
            .select()
            .where { ExternalAttendances.uuid eq uuid }
            .map { ExternalAttendances.createEntity(it) }
            .firstOrNull()
    }

    @Nested
    inner class InsertOperations {
        @Test
        fun `insert returns 1 when external attendance is successfully created`() {
            val externalAttendance =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 5)

            val insertResult = externalAttendancesService.insert(externalAttendance)

            assertEquals(1, insertResult, "Insert should return 1 for successful creation")
        }

        @Test
        fun `insert persists external attendance with the given count`() {
            val externalAttendance =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 12)

            externalAttendancesService.insert(externalAttendance)
            val retrieved = getExternalAttendanceByUuid(externalAttendance.uuid)

            assertEquals(externalAttendance.uuid, retrieved?.uuid)
            assertEquals(testScheduleUuid, retrieved?.scheduleUuid)
            assertEquals(12, retrieved?.attendancesCount)
            assertFalse(retrieved?.isDeleted == true, "isDeleted should be false")
        }

        @Test
        fun `insert persists external attendance with default zero count`() {
            val externalAttendance = anExternalAttendance(scheduleUuid = testScheduleUuid)

            val insertResult = externalAttendancesService.insert(externalAttendance)

            assertEquals(1, insertResult)
            val retrieved = getExternalAttendanceByUuid(externalAttendance.uuid)
            assertEquals(0, retrieved?.attendancesCount, "Default count should be 0")
        }

        @Test
        fun `insert persists external attendance with isDeleted true`() {
            val externalAttendance =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 3, isDeleted = true)

            externalAttendancesService.insert(externalAttendance)
            val retrieved = getExternalAttendanceByUuid(externalAttendance.uuid)

            assertEquals(true, retrieved?.isDeleted, "isDeleted should be true")
        }

        @Test
        fun `multiple external attendances can exist for the same schedule`() {
            val externalAttendance1 =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 2)
            val externalAttendance2 =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 4)

            val result1 = externalAttendancesService.insert(externalAttendance1)
            val result2 = externalAttendancesService.insert(externalAttendance2)

            assertEquals(1, result1)
            assertEquals(1, result2)
            assertEquals(2, getExternalAttendanceByUuid(externalAttendance1.uuid)?.attendancesCount)
            assertEquals(4, getExternalAttendanceByUuid(externalAttendance2.uuid)?.attendancesCount)
        }
    }

    @Nested
    inner class UpdateOperations {
        @Test
        fun `update returns 1 when external attendance is successfully updated`() {
            val externalAttendance =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 1)
            externalAttendancesService.insert(externalAttendance)

            val updateResult =
                externalAttendancesService.update(externalAttendance.uuid, UpdatedExternalAttendance(attendancesCount = 8))

            assertEquals(1, updateResult, "Update should return 1 for successful update")
        }

        @Test
        fun `update changes the attendances count`() {
            val externalAttendance =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 1)
            externalAttendancesService.insert(externalAttendance)

            externalAttendancesService.update(externalAttendance.uuid, UpdatedExternalAttendance(attendancesCount = 20))

            val retrieved = getExternalAttendanceByUuid(externalAttendance.uuid)
            assertEquals(20, retrieved?.attendancesCount, "Count should be updated to 20")
        }

        @Test
        fun `update can set the attendances count to zero`() {
            val externalAttendance =
                anExternalAttendance(scheduleUuid = testScheduleUuid, attendancesCount = 5)
            externalAttendancesService.insert(externalAttendance)

            externalAttendancesService.update(externalAttendance.uuid, UpdatedExternalAttendance(attendancesCount = 0))

            val retrieved = getExternalAttendanceByUuid(externalAttendance.uuid)
            assertEquals(0, retrieved?.attendancesCount, "Count should be updated to 0")
        }

        @Test
        fun `update returns 0 for non-existent external attendance`() {
            val nonExistentUuid = UUID.randomUUID()

            val updateResult =
                externalAttendancesService.update(nonExistentUuid, UpdatedExternalAttendance(attendancesCount = 5))

            assertEquals(0, updateResult, "Update should return 0 for non-existent external attendance")
        }
    }
}
