package com.lunatech.chef.api.schedulers

import com.lunatech.chef.api.domain.NewSchedule
import com.lunatech.chef.api.domain.RecurrentSchedule
import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import java.time.LocalDate
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext

private val logger = KotlinLogging.logger {}

class SchedulerJob(val schedulesService: SchedulesService, val recurrentSchedulesService: RecurrentSchedulesService) : Job {

    override fun execute(context: JobExecutionContext?) {
        logger.info("Let's rock and roll!")
        val today = LocalDate.now()
        val inAWeek = today.plusDays(7)

        val recSchedules = recurrentSchedulesService.getIntervalDate(today, inAWeek)

        // val data = context.getJobDetail().getJobDataMap()
        // val count = data.get()

        logger.info("Found ${recSchedules.size} recurrent schedules")
        for (rec in recSchedules) {
            val newSchedule = NewSchedule(menuUuid = rec.menuUuid, date = rec.nextDate, locationUuid = rec.locationUuid)
            schedulesService.insert(Schedule.fromNewSchedule(newSchedule))
            val updatedRecSchedule = RecurrentSchedule(
                uuid = rec.uuid,
                menuUuid = rec.menuUuid,
                locationUuid = rec.locationUuid,
                repetitionDays = rec.repetitionDays,
                nextDate = rec.nextDate.plusDays(rec.repetitionDays.toLong()))
            recurrentSchedulesService.insert(updatedRecSchedule)
        }
    }
}
