package com.lunatech.chef.api.slackbot

data class SlackUser(
    val id: String,
    val deleted: Boolean,
    val email: String?,
)

interface SlackApi {
    suspend fun usersList(): List<SlackUser>

    suspend fun openConversation(userId: String): String?

    suspend fun postMessage(
        channel: String,
        text: String,
        attachmentsJson: String,
    ): Boolean
}
