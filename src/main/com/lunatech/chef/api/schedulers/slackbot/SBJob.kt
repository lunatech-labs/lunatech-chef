package com.lunatech.chef.api.schedulers.slackbot

import com.lunatech.chef.api.slackbot.LunchReminderService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext

private val logger = KotlinLogging.logger {}

class SBJob : Job {
    companion object {
        const val LUNCH_REMINDER_SERVICE: String = "lunchReminderService"
    }

    override fun execute(context: JobExecutionContext?) {
        logger.info("Starting job that sends lunch reminders via Slack.")
        val dataMap = context!!.jobDetail.jobDataMap
        val lunchReminderService = dataMap[LUNCH_REMINDER_SERVICE] as LunchReminderService

        runBlocking { lunchReminderService.sendReminders() }
    }
}
