package com.lunatech.chef.api.schedulers.recurrentschedules

import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.CronTrigger
import org.quartz.JobBuilder.newJob
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger

fun rcSchedulerTrigger(
    scheduler: Scheduler,
    schedulesService: SchedulesService,
    recurrentSchedulesService: RecurrentSchedulesService,
    attendancesService: AttendancesService,
    cronExpression: String,
) {
    val job: JobDetail = newJob(RSJob::class.java)
        .withIdentity("recurrentSchedules", "chefSchedules")
        .build()

    job.jobDataMap[RSJob.schedulesService] = schedulesService
    job.jobDataMap[RSJob.recurrentSchedulesService] = recurrentSchedulesService
    job.jobDataMap[RSJob.attendancesService] = attendancesService

    val trigger: CronTrigger = newTrigger()
        .withIdentity("weekSchedules", "weekSchedulesTrigger")
        .withSchedule(cronSchedule(cronExpression))
        .build()
    scheduler.scheduleJob(job, trigger)
}
