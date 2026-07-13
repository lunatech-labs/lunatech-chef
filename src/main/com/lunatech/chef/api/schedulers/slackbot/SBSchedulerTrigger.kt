package com.lunatech.chef.api.schedulers.slackbot

import com.lunatech.chef.api.slackbot.LunchReminderService
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.CronTrigger
import org.quartz.JobBuilder.newJob
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger
import java.util.TimeZone

fun sbSchedulerTrigger(
    scheduler: Scheduler,
    lunchReminderService: LunchReminderService,
    cronExpression: String,
) {
    val job: JobDetail =
        newJob(SBJob::class.java)
            .withIdentity("slackLunchReminders", "chefSchedules")
            .build()

    job.jobDataMap[SBJob.LUNCH_REMINDER_SERVICE] = lunchReminderService

    val trigger: CronTrigger =
        newTrigger()
            .withIdentity("slackLunchReminders", "slackLunchRemindersTrigger")
            .withSchedule(cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("Europe/Amsterdam")))
            .build()
    scheduler.scheduleJob(job, trigger)
}
