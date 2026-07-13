package com.lunatech.chef.api.slackbot

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

/**
 * All user-facing strings and the legacy attachment payload, ported verbatim
 * from lunatech-slackbots lunch-bot (messages.properties and SlackDto).
 */
object SlackMessages {
    const val ACTION_NAME = "decision"
    const val SALUTATION =
        "Hello, LunchBot here. :robot_face:\nI would love to see you at the office. Are you joining us for lunch?"

    private val mapper = ObjectMapper()

    fun dayOfWeek(date: LocalDate): String = date.dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH)

    fun attachmentsJson(
        attendanceUuid: UUID,
        date: LocalDate,
        office: String,
        menuName: String,
    ): String {
        val day = dayOfWeek(date)
        val attachment =
            linkedMapOf(
                "mrkdwn_in" to listOf("text"),
                "text" to "*Day*: $day\n*Where*: $office\n*Menu*: $menuName",
                "callback_id" to "${office}_$day",
                "actions" to
                    listOf(
                        linkedMapOf(
                            "name" to ACTION_NAME,
                            "text" to "Yes, I'll be in $office!",
                            "style" to "primary",
                            "value" to "${attendanceUuid}_true",
                        ),
                        linkedMapOf(
                            "name" to ACTION_NAME,
                            "text" to "Nope. :no_good:",
                            "style" to "danger",
                            "value" to "${attendanceUuid}_false",
                        ),
                    ),
            )
        return mapper.writeValueAsString(listOf(attachment))
    }

    fun goingResponse(
        office: String,
        dayOfWeek: String,
        publicUrl: String,
    ): String = "Great, see you in $office on $dayOfWeek! :star-struck: If your plans change, please update your answer at $publicUrl."

    fun notGoingResponse(publicUrl: String): String = "Alright :cry: If your plans change, please update your answer at $publicUrl."

    fun errorResponse(publicUrl: String): String =
        "Something went wrong processing your response :cry: Please go directly to $publicUrl and add your answer there."
}
