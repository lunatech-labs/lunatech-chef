package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.routes.UpdatedAttendance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
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

class AttendancesServiceTest {
    private lateinit var attendancesService: AttendancesService
    private lateinit var usersService: UsersService
    private lateinit var schedulesService: SchedulesService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService

    private lateinit var testOfficeUuid: UUID
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

        // Create test office
        val testOffice = anOffice(city = "Rotterdam")
        officesService.insert(testOffice)
        testOfficeUuid = testOffice.uuid

        // Create test users
        val testUser = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
        val testUser2 = aUser(name = "Jane Doe", emailAddress = uniqueEmail("jane"), officeUuid = testOfficeUuid)
        usersService.insert(testUser)
        usersService.insert(testUser2)
        testUserUuid = testUser.uuid
        testUser2Uuid = testUser2.uuid

        // Create test dish and menu
        val testDish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(testDish)
        val testMenu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish.uuid))
        menusService.insert(testMenu)

        // Create test schedule
        val testSchedule = aSchedule(
            menuUuid = testMenu.uuid,
            date = LocalDate.now().plusDays(7),
            officeUuid = testOfficeUuid,
        )
        schedulesService.insert(testSchedule)
        testScheduleUuid = testSchedule.uuid
    }

    // Helper to retrieve attendance directly from database for testing
    private fun getAttendanceByUuid(uuid: UUID): Attendance? {
        val database = TestDatabase.getDatabase()
        return database.from(Attendances)
            .select()
            .where { Attendances.uuid eq uuid }
            .map { row ->
                Attendance(
                    uuid = row[Attendances.uuid]!!,
                    scheduleUuid = row[Attendances.scheduleUuid]!!,
                    userUuid = row[Attendances.userUuid]!!,
                    isAttending = row[Attendances.isAttending],
                    isDeleted = row[Attendances.isDeleted]!!,
                )
            }
            .firstOrNull()
    }

    // Helper to get all attendances for a schedule
    private fun getAttendancesBySchedule(scheduleUuid: UUID): List<Attendance> {
        val database = TestDatabase.getDatabase()
        return database.from(Attendances)
            .select()
            .where { Attendances.scheduleUuid eq scheduleUuid }
            .map { row ->
                Attendance(
                    uuid = row[Attendances.uuid]!!,
                    scheduleUuid = row[Attendances.scheduleUuid]!!,
                    userUuid = row[Attendances.userUuid]!!,
                    isAttending = row[Attendances.isAttending],
                    isDeleted = row[Attendances.isDeleted]!!,
                )
            }
    }

    @Nested
    inner class InsertOperations {
        @Test
        fun `insert returns 1 when attendance is successfully created`() {
            val attendance = anAttendance(
                scheduleUuid = testScheduleUuid,
                userUuid = testUserUuid,
                isAttending = true,
            )

            val insertResult = attendancesService.insert(attendance)

            assertEquals(1, insertResult, "Insert should return 1 for successful creation")
        }

        @Test
        fun `insert persists attendance with isAttending true`() {
            val attendance = anAttendance(
                scheduleUuid = testScheduleUuid,
                userUuid = testUserUuid,
                isAttending = true,
            )

            attendancesService.insert(attendance)
            val retrieved = getAttendanceByUuid(attendance.uuid)

            assertEquals(testScheduleUuid, retrieved?.scheduleUuid)
            assertEquals(testUserUuid, retrieved?.userUuid)
            assertTrue(retrieved?.isAttending == true, "isAttending should be true")
        }

        @Test
        fun `insert persists attendance with isAttending false`() {
            val attendance = anAttendance(
                scheduleUuid = testScheduleUuid,
                userUuid = testUserUuid,
                isAttending = false,
            )

            attendancesService.insert(attendance)
            val retrieved = getAttendanceByUuid(attendance.uuid)

            assertFalse(retrieved?.isAttending == true, "isAttending should be false")
        }

        @Test
        fun `insert persists attendance with null isAttending for undecided`() {
            val attendance = anAttendance(
                scheduleUuid = testScheduleUuid,
                userUuid = testUserUuid,
                isAttending = null,
            )

            val insertResult = attendancesService.insert(attendance)

            assertEquals(1, insertResult)
            val retrieved = getAttendanceByUuid(attendance.uuid)
            assertNull(retrieved?.isAttending, "isAttending should be null for undecided")
        }
    }

    @Nested
    inner class UpdateOperations {
        @Test
        fun `update returns 1 when attendance is successfully updated`() {
            val attendance = anAttendance(
                scheduleUuid = testScheduleUuid,
                userUuid = testUserUuid,
                isAttending = false,
            )
            attendancesService.insert(attendance)

            val updatedAttendance = UpdatedAttendance(isAttending = true)

            val updateResult = attendancesService.update(attendance.uuid, updatedAttendance)

            assertEquals(1, updateResult, "Update should return 1 for successful update")
        }

        @Test
        fun `update changes isAttending from false to true`() {
            val attendance = anAttendance(
                scheduleUuid = testScheduleUuid,
                userUuid = testUserUuid,
                isAttending = false,
            )
            attendancesService.insert(attendance)

            val updatedAttendance = UpdatedAttendance(isAttending = true)
            attendancesService.update(attendance.uuid, updatedAttendance)

            val retrieved = getAttendanceByUuid(attendance.uuid)
            assertTrue(retrieved?.isAttending == true, "isAttending should be updated to true")
        }

        @Test
        fun `update changes isAttending from true to false`() {
            val attendance = anAttendance(
                scheduleUuid = testScheduleUuid,
                userUuid = testUserUuid,
                isAttending = true,
            )
            attendancesService.insert(attendance)

            val updatedAttendance = UpdatedAttendance(isAttending = false)
            attendancesService.update(attendance.uuid, updatedAttendance)

            val retrieved = getAttendanceByUuid(attendance.uuid)
            assertFalse(retrieved?.isAttending == true, "isAttending should be updated to false")
        }

        @Test
        fun `update returns 0 for non-existent attendance`() {
            val nonExistentUuid = UUID.randomUUID()
            val updatedAttendance = UpdatedAttendance(isAttending = true)

            val updateResult = attendancesService.update(nonExistentUuid, updatedAttendance)

            assertEquals(0, updateResult, "Update should return 0 for non-existent attendance")
        }
    }

    @Nested
    inner class BulkInsertOperations {
        @Test
        fun `insertAttendanceAllUsers creates attendance for all active users`() {
            val insertResult = attendancesService.insertAttendanceAllUsers(testScheduleUuid, null)

            assertEquals(2, insertResult, "Should create attendance for all 2 users")
        }

        @Test
        fun `insertAttendanceAllUsers with isAttending true sets all to attending`() {
            attendancesService.insertAttendanceAllUsers(testScheduleUuid, true)

            val attendances = getAttendancesBySchedule(testScheduleUuid)
            assertEquals(2, attendances.size)
            assertTrue(attendances.all { it.isAttending == true }, "All attendances should be set to true")
        }

        @Test
        fun `insertAttendanceAllUsers with isAttending false sets all to not attending`() {
            attendancesService.insertAttendanceAllUsers(testScheduleUuid, false)

            val attendances = getAttendancesBySchedule(testScheduleUuid)
            assertEquals(2, attendances.size)
            assertTrue(attendances.all { it.isAttending == false }, "All attendances should be set to false")
        }

        @Test
        fun `insertAttendanceAllUsers with null isAttending sets all to undecided`() {
            attendancesService.insertAttendanceAllUsers(testScheduleUuid, null)

            val attendances = getAttendancesBySchedule(testScheduleUuid)
            assertEquals(2, attendances.size)
            assertTrue(attendances.all { it.isAttending == null }, "All attendances should be null (undecided)")
        }

        @Test
        fun `insertAttendanceForUser creates attendance for specific user`() {
            val insertResult = attendancesService.insertAttendanceForUser(testUserUuid, testScheduleUuid, true)

            assertEquals(1, insertResult, "Should create attendance for specific user")
        }

        @Test
        fun `insertAttendanceForUser with null isAttending creates undecided attendance`() {
            attendancesService.insertAttendanceForUser(testUserUuid, testScheduleUuid, null)

            val attendances = getAttendancesBySchedule(testScheduleUuid)
            assertEquals(1, attendances.size)
            assertNull(attendances[0].isAttending, "Attendance should be null (undecided)")
        }
    }

    @Nested
    inner class MultipleAttendanceScenarios {
        @Test
        fun `multiple users can have attendance for same schedule`() {
            val attendance1 = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUserUuid, isAttending = true)
            val attendance2 = anAttendance(scheduleUuid = testScheduleUuid, userUuid = testUser2Uuid, isAttending = false)

            val result1 = attendancesService.insert(attendance1)
            val result2 = attendancesService.insert(attendance2)

            assertEquals(1, result1, "First attendance should be created")
            assertEquals(1, result2, "Second attendance should be created")

            val retrieved1 = getAttendanceByUuid(attendance1.uuid)
            val retrieved2 = getAttendanceByUuid(attendance2.uuid)

            assertTrue(retrieved1?.isAttending == true)
            assertFalse(retrieved2?.isAttending == true)
        }
    }
}
