package com.lunatech.chef.api.schedulers

import com.lunatech.chef.api.domain.NewSchedule
import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.routes.UpdatedRecurrentSchedule
import java.time.LocalDate
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext

private val logger = KotlinLogging.logger {}

class SchedulerJob() : Job {

    companion object SchedulerJob {
        const val schedulesService: String = "schedulesService"
        const val recurrentSchedulesService: String = "recurrentSchedulesService"
    }

    override fun execute(context: JobExecutionContext?) {
        logger.info("Starting the job that updates recurrent schedules.")
        val today = LocalDate.now()
        val inAWeek = today.plusDays(7)

        val dataMap = context!!.jobDetail.jobDataMap

        val schedulesService: SchedulesService = dataMap.get(schedulesService) as SchedulesService
        val recurrentSchedulesService: RecurrentSchedulesService = dataMap.get(recurrentSchedulesService) as RecurrentSchedulesService

        val recSchedules = recurrentSchedulesService.getIntervalDate(today, inAWeek)

        logger.info("Found ${recSchedules.size} recurrent schedules.")
        for (rec in recSchedules) {
            val newSchedule = NewSchedule(menuUuid = rec.menuUuid, date = rec.nextDate, locationUuid = rec.locationUuid)
            schedulesService.insert(Schedule.fromNewSchedule(newSchedule))
            val updatedRecSchedule = UpdatedRecurrentSchedule(
                menuUuid = rec.menuUuid,
                locationUuid = rec.locationUuid,
                repetitionDays = rec.repetitionDays,
                nextDate = rec.nextDate.plusDays(rec.repetitionDays.toLong()))
            recurrentSchedulesService.update(rec.uuid, updatedRecSchedule)
        }
    }
}
