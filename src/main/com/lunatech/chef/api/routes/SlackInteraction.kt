package com.lunatech.chef.api.routes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.slackbot.SlackMessages
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackInteractionAction(
    val name: String? = null,
    val value: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackInteractionPayload(
    val callback_id: String? = null,
    val actions: List<SlackInteractionAction> = emptyList(),
)

/**
 * Receives legacy interactive-message button clicks from the LunchBot Slack app.
 * Unauthenticated by design: Slack cannot present a chef session (see the design spec).
 * The plain-text response replaces the original message in Slack.
 */
fun Route.slackInteraction(
    attendancesService: AttendancesService,
    publicUrl: String,
) {
    val mapper = jacksonObjectMapper()

    post("/slack") {
        val errorAck = SlackMessages.errorResponse(publicUrl)
        try {
            val rawPayload = call.receiveParameters()["payload"]
            if (rawPayload == null) {
                logger.error { "Slack interaction without payload parameter" }
                return@post call.respondText(errorAck)
            }
            val payload = mapper.readValue<SlackInteractionPayload>(rawPayload)

            val value = payload.actions.firstOrNull { it.name == SlackMessages.ACTION_NAME }?.value
            val valueParts = value?.split("_")
            val uuid = valueParts?.getOrNull(0).toUUIDOrNull()
            val isAttending = valueParts?.getOrNull(1)?.toBooleanStrictOrNull()
            if (valueParts?.size != 2 || uuid == null || isAttending == null) {
                val sanitizedValue = value?.replace("\n", "\\n")?.replace("\r", "\\r")?.take(100)
                logger.error { "Slack interaction with unusable action value: $sanitizedValue" }
                return@post call.respondText(errorAck)
            }

            val updated = attendancesService.update(uuid, UpdatedAttendance(isAttending))
            if (updated != 1) {
                logger.error { "Slack interaction could not update attendance $uuid" }
                return@post call.respondText(errorAck)
            }

            // day names contain no underscore, so split office/day from the last one:
            // office names themselves may contain underscores
            val callbackId = payload.callback_id
            val ack =
                when {
                    !isAttending -> SlackMessages.notGoingResponse(publicUrl)
                    callbackId != null && callbackId.contains("_") ->
                        SlackMessages.goingResponse(
                            callbackId.substringBeforeLast("_"),
                            callbackId.substringAfterLast("_"),
                            publicUrl,
                        )
                    else -> errorAck
                }
            call.respondText(ack)
        } catch (exception: Exception) {
            logger.error("Error processing Slack interaction", exception)
            call.respondText(errorAck)
        }
    }
}
