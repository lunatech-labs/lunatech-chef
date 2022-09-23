package com.lunatech.chef.api.schedulers

import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.CronTrigger
import org.quartz.JobBuilder.newJob
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger

fun schedulerTrigger(scheduler: Scheduler, schedulesService: SchedulesService, recurrentSchedulesService: RecurrentSchedulesService) {
    val job: JobDetail = newJob(SchedulerJob::class.java)
        .withIdentity("recurrentSchedules", "chefSchedules")
        .usi
        .build()

    val trigger: CronTrigger = newTrigger()
        .withIdentity("weekSchedules", "weekSchedulesTrigger")
        .withSchedule(cronSchedule("* */10 * ? * *")) // every 10 minutes
        .build()
    scheduler.scheduleJob(job, trigger)
}
