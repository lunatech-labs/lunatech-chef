package com.lunatech.chef.api.config

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SlackBotConfigTest {
    @Test
    fun `parses slackbot config block`() {
        val config =
            ConfigFactory.parseString(
                """
                slackbot {
                  token = "xoxp-something"
                  cron = "0 0 10 ? * MON,TUE"
                  publicUrl = "https://lunch.lunatech.nl"
                }
                """.trimIndent(),
            )

        val slackBotConfig = SlackBotConfig.fromConfig(config.getConfig("slackbot"))

        assertEquals("xoxp-something", slackBotConfig.token)
        assertEquals("0 0 10 ? * MON,TUE", slackBotConfig.cron)
        assertEquals("https://lunch.lunatech.nl", slackBotConfig.publicUrl)
    }
}
