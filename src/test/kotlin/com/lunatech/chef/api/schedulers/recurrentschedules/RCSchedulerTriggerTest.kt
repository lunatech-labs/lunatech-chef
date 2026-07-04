package com.lunatech.chef.api.schedulers.recurrentschedules

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.ExternalAttendancesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.quartz.CronTrigger
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerKey
import org.quartz.impl.StdSchedulerFactory

class RCSchedulerTriggerTest {
    private lateinit var scheduler: Scheduler
    private lateinit var schedulesService: SchedulesService
    private lateinit var recurrentSchedulesService: RecurrentSchedulesService
    private lateinit var attendancesService: AttendancesService
    private lateinit var externalAttendancesService: ExternalAttendancesService

    private val jobKey = JobKey("recurrentSchedules", "chefSchedules")
    private val triggerKey = TriggerKey("weekSchedules", "weekSchedulesTrigger")
    private val cronExpression = "0 0 5 * * ?"

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        schedulesService = SchedulesService(database)
        recurrentSchedulesService = RecurrentSchedulesService(database)
        val usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService)
        externalAttendancesService = ExternalAttendancesService(database)

        // In-memory scheduler; it is never started so no job actually runs
        scheduler = StdSchedulerFactory().scheduler
    }

    @AfterEach
    fun tearDown() {
        scheduler.shutdown()
    }

    @Test
    fun `rcSchedulerTrigger schedules the RSJob with the expected identity`() {
        rcSchedulerTrigger(
            scheduler,
            schedulesService,
            recurrentSchedulesService,
            attendancesService,
            externalAttendancesService,
            cronExpression,
        )

        val jobDetail = scheduler.getJobDetail(jobKey)
        assertNotNull(jobDetail, "Job should be registered with identity recurrentSchedules/chefSchedules")
        assertEquals(RSJob::class.java, jobDetail.jobClass, "Job should execute RSJob")
    }

    @Test
    fun `rcSchedulerTrigger stores all services in the job data map`() {
        rcSchedulerTrigger(
            scheduler,
            schedulesService,
            recurrentSchedulesService,
            attendancesService,
            externalAttendancesService,
            cronExpression,
        )

        val jobDataMap = scheduler.getJobDetail(jobKey).jobDataMap
        assertSame(schedulesService, jobDataMap[RSJob.SCHEDULES_SERVICE])
        assertSame(recurrentSchedulesService, jobDataMap[RSJob.RECURRENT_SCHEDULES_SERVICE])
        assertSame(attendancesService, jobDataMap[RSJob.ATTENDANCES_SERVICE])
        assertSame(externalAttendancesService, jobDataMap[RSJob.EXTERNAL_ATTENDANCES_SERVICE])
    }

    @Test
    fun `rcSchedulerTrigger creates a cron trigger with the given expression`() {
        rcSchedulerTrigger(
            scheduler,
            schedulesService,
            recurrentSchedulesService,
            attendancesService,
            externalAttendancesService,
            cronExpression,
        )

        val trigger = scheduler.getTrigger(triggerKey)
        assertNotNull(trigger, "Trigger should be registered with identity weekSchedules/weekSchedulesTrigger")
        assertTrue(trigger is CronTrigger, "Trigger should be a cron trigger")
        assertEquals(cronExpression, (trigger as CronTrigger).cronExpression)
        assertEquals(jobKey, trigger.jobKey, "Trigger should be associated with the recurrent schedules job")
    }

    @Test
    fun `rcSchedulerTrigger throws for an invalid cron expression`() {
        assertThrows(RuntimeException::class.java) {
            rcSchedulerTrigger(
                scheduler,
                schedulesService,
                recurrentSchedulesService,
                attendancesService,
                externalAttendancesService,
                "not a cron expression",
            )
        }
    }
}
