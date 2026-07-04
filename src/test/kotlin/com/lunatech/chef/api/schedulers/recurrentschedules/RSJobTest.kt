package com.lunatech.chef.api.schedulers.recurrentschedules

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aRecurrentSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.ExternalAttendances
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.ExternalAttendancesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.quartz.JobBuilder.newJob
import org.quartz.impl.JobExecutionContextImpl
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.quartz.spi.TriggerFiredBundle
import java.time.LocalDate
import java.util.Date
import java.util.UUID

class RSJobTest {
    private lateinit var schedulesService: SchedulesService
    private lateinit var recurrentSchedulesService: RecurrentSchedulesService
    private lateinit var attendancesService: AttendancesService
    private lateinit var externalAttendancesService: ExternalAttendancesService
    private lateinit var usersService: UsersService
    private lateinit var menusService: MenusService
    private lateinit var officesService: OfficesService
    private lateinit var dishesService: DishesService

    private lateinit var testOfficeUuid: UUID
    private lateinit var testMenuUuid: UUID
    private lateinit var testUserUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()

        officesService = OfficesService(database)
        dishesService = DishesService(database)
        menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        recurrentSchedulesService = RecurrentSchedulesService(database)
        usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService)
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
        testMenuUuid = testMenu.uuid

        // Create test user
        val testUser = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
        usersService.insert(testUser)
        testUserUuid = testUser.uuid
    }

    // Helper to execute the job with a real Quartz context, the same way the scheduler would
    private fun executeJob() {
        val jobDetail =
            newJob(RSJob::class.java)
                .withIdentity("recurrentSchedules", "chefSchedules")
                .build()
        jobDetail.jobDataMap[RSJob.SCHEDULES_SERVICE] = schedulesService
        jobDetail.jobDataMap[RSJob.RECURRENT_SCHEDULES_SERVICE] = recurrentSchedulesService
        jobDetail.jobDataMap[RSJob.ATTENDANCES_SERVICE] = attendancesService
        jobDetail.jobDataMap[RSJob.EXTERNAL_ATTENDANCES_SERVICE] = externalAttendancesService

        val firedBundle =
            TriggerFiredBundle(jobDetail, SimpleTriggerImpl(), null, false, Date(), null, null, null)
        val context = JobExecutionContextImpl(null, firedBundle, RSJob())

        RSJob().execute(context)
    }

    // Helper to retrieve attendances directly from database.
    // Rows are mapped manually because Attendances.createEntity turns a null isAttending into false.
    private fun getAttendancesByScheduleUuid(scheduleUuid: UUID) =
        TestDatabase
            .getDatabase()
            .from(Attendances)
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

    // Helper to retrieve external attendances directly from database
    private fun getExternalAttendancesByScheduleUuid(scheduleUuid: UUID) =
        TestDatabase
            .getDatabase()
            .from(ExternalAttendances)
            .select()
            .where { ExternalAttendances.scheduleUuid eq scheduleUuid }
            .map { ExternalAttendances.createEntity(it) }

    @Nested
    inner class ScheduleCreation {
        @Test
        fun `execute creates a schedule for a recurrent schedule within the next week`() {
            val nextDate = LocalDate.now().plusDays(3)
            val recurrentSchedule =
                aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = nextDate)
            recurrentSchedulesService.insert(recurrentSchedule)

            executeJob()

            val schedules = schedulesService.getAll()
            assertEquals(1, schedules.size, "One schedule should be created")
            assertEquals(testMenuUuid, schedules[0].menuUuid)
            assertEquals(testOfficeUuid, schedules[0].officeUuid)
            assertEquals(nextDate, schedules[0].date, "Schedule date should be the recurrent schedule's next date")
        }

        @Test
        fun `execute creates undecided attendances for all users for the new schedule`() {
            val secondUser = aUser(name = "Jane Doe", emailAddress = uniqueEmail("jane"), officeUuid = testOfficeUuid)
            usersService.insert(secondUser)

            val recurrentSchedule =
                aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = LocalDate.now().plusDays(3))
            recurrentSchedulesService.insert(recurrentSchedule)

            executeJob()

            val schedule = schedulesService.getAll()[0]
            val attendances = getAttendancesByScheduleUuid(schedule.uuid)
            assertEquals(2, attendances.size, "An attendance should be created for each user")
            attendances.forEach { attendance ->
                assertNull(attendance.isAttending, "Attendances should be created as undecided")
            }
            assertTrue(attendances.any { it.userUuid == testUserUuid })
            assertTrue(attendances.any { it.userUuid == secondUser.uuid })
        }

        @Test
        fun `execute creates an external attendance with zero count for the new schedule`() {
            val recurrentSchedule =
                aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = LocalDate.now().plusDays(3))
            recurrentSchedulesService.insert(recurrentSchedule)

            executeJob()

            val schedule = schedulesService.getAll()[0]
            val externalAttendances = getExternalAttendancesByScheduleUuid(schedule.uuid)
            assertEquals(1, externalAttendances.size, "One external attendance should be created")
            assertEquals(0, externalAttendances[0].attendancesCount, "External attendance should start with count 0")
        }

        @Test
        fun `execute advances the next date of the recurrent schedule by its repetition days`() {
            val nextDate = LocalDate.now().plusDays(3)
            val recurrentSchedule =
                aRecurrentSchedule(
                    menuUuid = testMenuUuid,
                    officeUuid = testOfficeUuid,
                    repetitionDays = 7,
                    nextDate = nextDate,
                )
            recurrentSchedulesService.insert(recurrentSchedule)

            executeJob()

            val updated = recurrentSchedulesService.getByUuid(recurrentSchedule.uuid)[0]
            assertEquals(
                nextDate.plusDays(7),
                updated.nextDate,
                "Next date should be advanced by the repetition days",
            )
        }

        @Test
        fun `execute processes multiple recurrent schedules within the next week`() {
            val recurrentSchedule1 =
                aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = LocalDate.now().plusDays(2))
            val recurrentSchedule2 =
                aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = LocalDate.now().plusDays(6))
            recurrentSchedulesService.insert(recurrentSchedule1)
            recurrentSchedulesService.insert(recurrentSchedule2)

            executeJob()

            val schedules = schedulesService.getAll()
            assertEquals(2, schedules.size, "A schedule should be created for each recurrent schedule")
        }
    }

    @Nested
    inner class SchedulesOutsideWindow {
        @Test
        fun `execute ignores recurrent schedules with next date beyond a week`() {
            val nextDate = LocalDate.now().plusDays(10)
            val recurrentSchedule =
                aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = nextDate)
            recurrentSchedulesService.insert(recurrentSchedule)

            executeJob()

            assertTrue(schedulesService.getAll().isEmpty(), "No schedule should be created")
            val unchanged = recurrentSchedulesService.getByUuid(recurrentSchedule.uuid)[0]
            assertEquals(nextDate, unchanged.nextDate, "Next date should not be changed")
        }

        @Test
        fun `execute ignores recurrent schedules with next date today`() {
            val recurrentSchedule =
                aRecurrentSchedule(menuUuid = testMenuUuid, officeUuid = testOfficeUuid, nextDate = LocalDate.now())
            recurrentSchedulesService.insert(recurrentSchedule)

            executeJob()

            assertTrue(schedulesService.getAll().isEmpty(), "No schedule should be created for today's date")
        }

        @Test
        fun `execute ignores deleted recurrent schedules`() {
            val recurrentSchedule =
                aRecurrentSchedule(
                    menuUuid = testMenuUuid,
                    officeUuid = testOfficeUuid,
                    nextDate = LocalDate.now().plusDays(3),
                    isDeleted = true,
                )
            recurrentSchedulesService.insert(recurrentSchedule)

            executeJob()

            assertTrue(schedulesService.getAll().isEmpty(), "No schedule should be created for a deleted recurrent schedule")
        }

        @Test
        fun `execute does nothing when there are no recurrent schedules`() {
            executeJob()

            assertTrue(schedulesService.getAll().isEmpty(), "No schedules should be created")
        }
    }
}
