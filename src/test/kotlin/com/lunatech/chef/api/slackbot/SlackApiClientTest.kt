package com.lunatech.chef.api.slackbot

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.parseUrlEncodedParameters
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SlackApiClientTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun clientReturning(
        json: String,
        recorder: (io.ktor.client.request.HttpRequestData) -> Unit = {},
    ): HttpClient =
        HttpClient(
            MockEngine { request ->
                recorder(request)
                respond(json, HttpStatusCode.OK, jsonHeaders)
            },
        )

    @Test
    fun `usersList parses members and sends bearer token`() =
        runBlocking {
            var authHeader: String? = null
            val http =
                clientReturning(
                    """{"ok":true,"members":[
                        {"id":"U1","deleted":false,"profile":{"email":"a@lunatech.nl"}},
                        {"id":"U2","deleted":true,"profile":{"email":"gone@lunatech.nl"}},
                        {"id":"U3","deleted":false,"profile":{}}
                    ]}""",
                ) { request -> authHeader = request.headers[HttpHeaders.Authorization] }

            val users = SlackApiClient("xoxp-test", http).usersList()

            assertEquals("Bearer xoxp-test", authHeader)
            assertEquals(3, users.size)
            assertEquals(SlackUser("U1", false, "a@lunatech.nl"), users[0])
            assertEquals(SlackUser("U3", false, null), users[2])
        }

    @Test
    fun `openConversation posts user id and returns channel id`() =
        runBlocking {
            var body: String? = null
            val http =
                clientReturning("""{"ok":true,"channel":{"id":"D123"}}""") { request ->
                    body = String((request.body as io.ktor.http.content.OutgoingContent.ByteArrayContent).bytes())
                }

            val channel = SlackApiClient("xoxp-test", http).openConversation("U1")

            assertEquals("D123", channel)
            assertEquals("U1", body!!.parseUrlEncodedParameters()["users"])
        }

    @Test
    fun `openConversation returns null when slack says not ok`() =
        runBlocking {
            val http = clientReturning("""{"ok":false,"error":"user_not_found"}""")

            assertNull(SlackApiClient("xoxp-test", http).openConversation("U1"))
        }

    @Test
    fun `postMessage sends channel, text and attachments as form fields`() =
        runBlocking {
            var body: String? = null
            val http =
                clientReturning("""{"ok":true}""") { request ->
                    body = String((request.body as io.ktor.http.content.OutgoingContent.ByteArrayContent).bytes())
                }

            val ok = SlackApiClient("xoxp-test", http).postMessage("D123", "hello", """[{"text":"x"}]""")

            assertTrue(ok)
            val params = body!!.parseUrlEncodedParameters()
            assertEquals("D123", params["channel"])
            assertEquals("hello", params["text"])
            assertEquals("""[{"text":"x"}]""", params["attachments"])
        }
}
