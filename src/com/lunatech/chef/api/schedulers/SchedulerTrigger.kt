package com.lunatech.chef.api.schedulers

import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.CronTrigger
import org.quartz.JobBuilder.newJob
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger

fun schedulerTrigger(scheduler: Scheduler, schedulesService: SchedulesService, recurrentSchedulesService: RecurrentSchedulesService, cronExpression: String) {
    val job: JobDetail = newJob(SchedulerJob::class.java)
        .withIdentity("recurrentSchedules", "chefSchedules")
        .build()

    job.jobDataMap.put(SchedulerJob.schedulesService, schedulesService)
    job.jobDataMap.put(SchedulerJob.recurrentSchedulesService, recurrentSchedulesService)

    val trigger: CronTrigger = newTrigger()
        .withIdentity("weekSchedules", "weekSchedulesTrigger")
        .withSchedule(cronSchedule(cronExpression))
        .build()
    scheduler.scheduleJob(job, trigger)
}
