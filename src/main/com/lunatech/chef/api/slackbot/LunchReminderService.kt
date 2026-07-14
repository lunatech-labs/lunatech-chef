package com.lunatech.chef.api.slackbot

import com.lunatech.chef.api.persistence.services.AttendancesForSlackbotService
import mu.KotlinLogging
import java.time.LocalDate
import java.time.ZoneId

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

    suspend fun sendReminders(today: LocalDate = LocalDate.now(ZoneId.of("Europe/Amsterdam"))) {
        val missing = attendancesForSlackbotService.getMissingAttendances(today, today.plusDays(DAYS_SPAN))
        if (missing.isEmpty()) {
            logger.info { "Lunchbot will not send any message. No attendances to be answered were found." }
            return
        }
        logger.info { "Lunchbot should send a total of ${missing.size} messages" }

        val byEmail = missing.groupBy { it.emailAddress }
        val slackUsers = slackApi.usersList()
        if (slackUsers.isEmpty()) {
            logger.error { "Slack users.list returned no users, aborting reminder run" }
            return
        }
        // email casing differs between chef and Slack profiles for some users
        val slackIdByEmail =
            slackUsers
                .filter { !it.deleted && it.email != null }
                .associate { it.email!!.lowercase() to it.id }

        for ((email, attendances) in byEmail) {
            val slackId = slackIdByEmail[email.lowercase()]
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
                    val sent =
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
                    if (!sent) {
                        logger.error { "Slack chat.postMessage failed for $email, continuing with remaining attendances" }
                    }
                }
            } catch (exception: Exception) {
                logger.error(exception) { "Error sending lunch reminders to $email, continuing with remaining users" }
            }
        }
    }
}
