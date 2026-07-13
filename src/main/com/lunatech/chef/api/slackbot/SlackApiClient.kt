package com.lunatech.chef.api.slackbot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class UsersListResponse(
    val ok: Boolean,
    val members: List<Member> = emptyList(),
    val error: String? = null,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Member(
        val id: String,
        val deleted: Boolean = false,
        val profile: Profile = Profile(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Profile(
        val email: String? = null,
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class OpenConversationResponse(
    val ok: Boolean,
    val channel: Channel? = null,
    val error: String? = null,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Channel(
        val id: String? = null,
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class PostMessageResponse(
    val ok: Boolean,
    val error: String? = null,
)

class SlackApiClient(
    private val token: String,
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://slack.com/api",
) : SlackApi {
    private val mapper = jacksonObjectMapper()

    override suspend fun usersList(): List<SlackUser> {
        val response: UsersListResponse = mapper.readValue(call("users.list"))
        if (!response.ok) {
            logger.error { "Slack users.list failed: ${response.error}" }
            return emptyList()
        }
        return response.members.map { SlackUser(it.id, it.deleted, it.profile.email) }
    }

    override suspend fun openConversation(userId: String): String? {
        val response: OpenConversationResponse = mapper.readValue(call("conversations.open", "users" to userId))
        if (!response.ok) {
            logger.error { "Slack conversations.open failed for $userId: ${response.error}" }
            return null
        }
        return response.channel?.id
    }

    override suspend fun postMessage(
        channel: String,
        text: String,
        attachmentsJson: String,
    ): Boolean {
        val response: PostMessageResponse =
            mapper.readValue(
                call("chat.postMessage", "channel" to channel, "text" to text, "attachments" to attachmentsJson),
            )
        if (!response.ok) {
            logger.error { "Slack chat.postMessage failed for channel $channel: ${response.error}" }
        }
        return response.ok
    }

    private suspend fun call(
        method: String,
        vararg formFields: Pair<String, String>,
    ): String =
        httpClient
            .submitForm(
                url = "$baseUrl/$method",
                formParameters = parameters { formFields.forEach { (key, value) -> append(key, value) } },
            ) { bearerAuth(token) }
            .bodyAsText()
}
