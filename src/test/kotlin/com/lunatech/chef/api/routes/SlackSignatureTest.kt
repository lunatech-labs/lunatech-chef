package com.lunatech.chef.api.routes

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Known-answer test against the worked example in Slack's own documentation
 * (docs.slack.dev/authentication/verifying-requests-from-slack), proving the
 * implementation is compatible with real Slack signatures rather than merely
 * self-consistent with the test helpers.
 */
class SlackSignatureTest {
    private val secret = "8f742231b10e8888abcd99yyyzzz85a5"
    private val timestamp = "1531420618"
    private val body =
        "token=xyzz0WbapA4vBCDEFasx0q6G&team_id=T1DC2JH3J&team_domain=testteamnow" +
            "&channel_id=G8PSS9T3V&channel_name=foobar&user_id=U2CERLKJA&user_name=roadrunner" +
            "&command=%2Fwebhook-collect&text=&response_url=https%3A%2F%2Fhooks.slack.com" +
            "%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN" +
            "&trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c"
    private val documentedSignature = "v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503"

    @Test
    fun `accepts slack's documented example signature`() {
        assertTrue(
            isValidSlackSignature(secret, timestamp, body, documentedSignature, nowEpochSeconds = timestamp.toLong()),
        )
    }

    @Test
    fun `rejects the documented example once the timestamp is stale`() {
        assertFalse(
            isValidSlackSignature(secret, timestamp, body, documentedSignature, nowEpochSeconds = timestamp.toLong() + 301),
        )
    }

    @Test
    fun `rejects a tampered body`() {
        assertFalse(
            isValidSlackSignature(secret, timestamp, body + "x", documentedSignature, nowEpochSeconds = timestamp.toLong()),
        )
    }

    @Test
    fun `rejects when the signing secret is empty`() {
        assertFalse(
            isValidSlackSignature("", timestamp, body, documentedSignature, nowEpochSeconds = timestamp.toLong()),
        )
    }
}
