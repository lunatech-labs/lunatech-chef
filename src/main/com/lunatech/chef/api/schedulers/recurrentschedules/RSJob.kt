package com.lunatech.chef.api.schedulers.recurrentschedules

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

class RSJob : Job {
    companion object {
        const val SCHEDULES_SERVICE: String = "schedulesService"
        const val RECURRENT_SCHEDULES_SERVICE: String = "recurrentSchedulesService"
        const val ATTENDANCES_SERVICE: String = "attendancesService"
    }

    override fun execute(context: JobExecutionContext?) {
        logger.info("Starting job that updates recurrent schedules.")
        val today = LocalDate.now()
        val inAWeek = today.plusDays(7)

        val dataMap = context!!.jobDetail.jobDataMap

        val schedulesService: SchedulesService = dataMap[SCHEDULES_SERVICE] as SchedulesService
        val recurrentSchedulesService: RecurrentSchedulesService =
            dataMap[RECURRENT_SCHEDULES_SERVICE] as RecurrentSchedulesService
        val attendancesService: AttendancesService = dataMap[ATTENDANCES_SERVICE] as AttendancesService

        val recSchedules = recurrentSchedulesService.getIntervalDate(today, inAWeek)

        logger.info("Found ${recSchedules.size} recurrent schedules to be created.")
        for (rec in recSchedules) {
            val newSchedule = NewSchedule(menuUuid = rec.menuUuid, date = rec.nextDate, officeUuid = rec.officeUuid)
            val dbSchedule = Schedule.fromNewSchedule(newSchedule)
            val isInserted = schedulesService.insert(dbSchedule)

            if (isInserted == 1) {
                attendancesService.insertAttendanceAllUsers(dbSchedule.uuid, isAttending = null)
                val updatedRecSchedule =
                    UpdatedRecurrentSchedule(
                        menuUuid = rec.menuUuid,
                        officeUuid = rec.officeUuid,
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
