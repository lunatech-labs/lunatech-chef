package com.lunatech.chef.api.slackbot

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class SlackMessagesTest {
    private val mapper = ObjectMapper()

    @Test
    fun `builds the exact legacy attachment payload`() {
        val uuid = UUID.fromString("11111111-2222-3333-4444-555555555555")
        // 2026-07-20 is a Monday
        val json = SlackMessages.attachmentsJson(uuid, LocalDate.of(2026, 7, 20), "Rotterdam", "Pasta Day")

        val expected =
            """
            [{"mrkdwn_in":["text"],
              "text":"*Day*: Monday\n*Where*: Rotterdam\n*Menu*: Pasta Day",
              "callback_id":"Rotterdam_Monday",
              "actions":[
                {"name":"decision","text":"Yes, I'll be in Rotterdam!","type":"button","style":"primary","value":"11111111-2222-3333-4444-555555555555_true"},
                {"name":"decision","text":"Nope. :no_good:","type":"button","style":"danger","value":"11111111-2222-3333-4444-555555555555_false"}
              ]}]
            """.trimIndent()

        // compare as trees so whitespace does not matter, but field values and order-sensitive arrays do
        assertEquals(mapper.readTree(expected), mapper.readTree(json))
    }

    @Test
    fun `dayOfWeek returns the full English day name for a non-Monday date`() {
        assertEquals("Saturday", SlackMessages.dayOfWeek(LocalDate.of(2026, 7, 25)))
    }

    @Test
    fun `salutation matches the java bot`() {
        assertEquals(
            "Hello, LunchBot here. :robot_face:\nI would love to see you at the office. Are you joining us for lunch?",
            SlackMessages.SALUTATION,
        )
    }

    @Test
    fun `acknowledgement strings match the java bot`() {
        val url = "https://lunch.lunatech.nl"
        assertEquals(
            "Great, see you in Rotterdam on Monday! :star-struck: If your plans change, please update your answer at $url.",
            SlackMessages.goingResponse("Rotterdam", "Monday", url),
        )
        assertEquals(
            "Alright :cry: If your plans change, please update your answer at $url.",
            SlackMessages.notGoingResponse(url),
        )
        assertEquals(
            "Something went wrong processing your response :cry: Please go directly to $url and add your answer there.",
            SlackMessages.errorResponse(url),
        )
    }
}
