package com.lunatech.chef.api.slackbot

import com.lunatech.chef.api.persistence.services.AttendancesForSlackbotService
import mu.KotlinLogging
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

/**
 * Sends the lunch reminder DMs previously sent by the external lunch-bot.
 * Looks 4 days ahead, matching the old bot: on Monday it covers Monday to Friday.
 */
class LunchReminderService(
    private val attendancesForSlackbotService: AttendancesForSlackbotService,
    private val slackApi: SlackApi,
) {
    companion object {
        private const val DAYS_SPAN = 4L
    }

    suspend fun sendReminders(today: LocalDate = LocalDate.now()) {
        val missing = attendancesForSlackbotService.getMissingAttendances(today, today.plusDays(DAYS_SPAN))
        if (missing.isEmpty()) {
            logger.info { "Lunchbot will not send any message. No attendances to be answered were found." }
            return
        }
        logger.info { "Lunchbot should send a total of ${missing.size} messages" }

        val byEmail = missing.groupBy { it.emailAddress }
        val slackIdByEmail =
            slackApi
                .usersList()
                .filter { !it.deleted && it.email != null }
                .associate { it.email!! to it.id }

        for ((email, attendances) in byEmail) {
            val slackId = slackIdByEmail[email]
            if (slackId == null) {
                logger.warn { "No Slack match for $email, skipping" }
                continue
            }
            try {
                val channel = slackApi.openConversation(slackId)
                if (channel == null) {
                    logger.error { "Could not open a Slack conversation for $email" }
                    continue
                }
                for (attendance in attendances) {
                    slackApi.postMessage(
                        channel,
                        SlackMessages.SALUTATION,
                        SlackMessages.attachmentsJson(
                            attendance.attendanceUuid,
                            attendance.date,
                            attendance.office,
                            attendance.menuName,
                        ),
                    )
                }
            } catch (exception: Exception) {
                logger.error(exception) { "Error sending lunch reminders to $email, continuing with remaining users" }
            }
        }
    }
}
