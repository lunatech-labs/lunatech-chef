package com.lunatech.chef.api.schedulers

import com.lunatech.chef.api.domain.NewSchedule
import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.routes.UpdatedRecurrentSchedule
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

class MealSchedulerJob() : Job {

    companion object SchedulerJob {
        const val schedulesService: String = "schedulesService"
        const val recurrentSchedulesService: String = "recurrentSchedulesService"
        const val attendancesService: String = "attendancesService"
    }

    override fun execute(context: JobExecutionContext?) {
        logger.info("Starting the job that updates recurrent schedules.")
        val today = LocalDate.now()
        val inAWeek = today.plusDays(7)

        val dataMap = context!!.jobDetail.jobDataMap

        val schedulesService: SchedulesService = dataMap.get(schedulesService) as SchedulesService
        val recurrentSchedulesService: RecurrentSchedulesService =
            dataMap.get(recurrentSchedulesService) as RecurrentSchedulesService
        val attendancesService: AttendancesService = dataMap.get(attendancesService) as AttendancesService

        val recSchedules = recurrentSchedulesService.getIntervalDate(today, inAWeek)

        logger.info("Found ${recSchedules.size} recurrent schedules to be created.")
        for (rec in recSchedules) {
            val newSchedule = NewSchedule(menuUuid = rec.menuUuid, date = rec.nextDate, locationUuid = rec.locationUuid)
            val dbSchedule = Schedule.fromNewSchedule(newSchedule)
            val isInserted = schedulesService.insert(dbSchedule)

            if (isInserted == 1) {
                attendancesService.insertAttendanceAllUsers(dbSchedule.uuid, isAttending = null)
                val updatedRecSchedule = UpdatedRecurrentSchedule(
                    menuUuid = rec.menuUuid,
                    locationUuid = rec.locationUuid,
                    repetitionDays = rec.repetitionDays,
                    nextDate = rec.nextDate.plusDays(rec.repetitionDays.toLong()),
                )
                recurrentSchedulesService.update(rec.uuid, updatedRecSchedule)
            } else {
                logger.error("Failed to create recurrent schedule for $rec")
            }
        }
    }
}
