package com.lunatech.chef.api.schedulers

import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.CronTrigger
import org.quartz.JobBuilder.newJob
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger

fun mealSchedulerTrigger(
    scheduler: Scheduler,
    schedulesService: SchedulesService,
    recurrentSchedulesService: RecurrentSchedulesService,
    attendancesService: AttendancesService,
    cronExpression: String,
) {
    val job: JobDetail = newJob(MealSchedulerJob::class.java)
        .withIdentity("recurrentSchedules", "chefSchedules")
        .build()

    job.jobDataMap[MealSchedulerJob.schedulesService] = schedulesService
    job.jobDataMap[MealSchedulerJob.recurrentSchedulesService] = recurrentSchedulesService
    job.jobDataMap[MealSchedulerJob.attendancesService] = attendancesService

    val trigger: CronTrigger = newTrigger()
        .withIdentity("weekSchedules", "weekSchedulesTrigger")
        .withSchedule(cronSchedule(cronExpression))
        .build()
    scheduler.scheduleJob(job, trigger)
}
