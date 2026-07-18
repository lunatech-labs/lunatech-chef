package com.lunatech.chef.api.routes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.slackbot.SlackMessages
import io.ktor.http.HttpStatusCode
import io.ktor.http.parseUrlEncodedParameters
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

private val logger = KotlinLogging.logger {}

private const val SIGNATURE_VERSION = "v0"
private const val MAX_TIMESTAMP_SKEW_SECONDS = 300L

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
 * Verifies Slack's request signature: HMAC-SHA256 of "v0:{timestamp}:{body}"
 * with the app's signing secret, compared in constant time. Rejects requests
 * older than 5 minutes to prevent replays. Fails closed on an empty secret.
 */
fun isValidSlackSignature(
    signingSecret: String,
    timestamp: String?,
    body: String,
    signature: String?,
    nowEpochSeconds: Long = Instant.now().epochSecond,
): Boolean {
    if (signingSecret.isEmpty() || timestamp == null || signature == null) return false
    val requestSeconds = timestamp.toLongOrNull() ?: return false
    if (abs(nowEpochSeconds - requestSeconds) > MAX_TIMESTAMP_SKEW_SECONDS) return false

    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(signingSecret.toByteArray(), "HmacSHA256"))
    val digest = mac.doFinal("$SIGNATURE_VERSION:$timestamp:$body".toByteArray())
    val computed = "$SIGNATURE_VERSION=" + digest.joinToString("") { "%02x".format(it) }
    return MessageDigest.isEqual(computed.toByteArray(), signature.toByteArray())
}

/**
 * Receives legacy interactive-message button clicks from the LunchBot Slack app.
 * Authenticated by Slack's signing secret instead of a chef session.
 * The plain-text response replaces the original message in Slack.
 */
fun Route.slackInteraction(
    attendancesService: AttendancesService,
    publicUrl: String,
    signingSecret: String,
) {
    val mapper = jacksonObjectMapper()

    post("/slack") {
        val errorAck = SlackMessages.errorResponse(publicUrl)
        try {
            val rawBody = call.receiveText()
            val timestamp = call.request.headers["X-Slack-Request-Timestamp"]
            val signature = call.request.headers["X-Slack-Signature"]
            if (!isValidSlackSignature(signingSecret, timestamp, rawBody, signature)) {
                logger.warn { "Rejected Slack interaction with a missing or invalid signature" }
                return@post call.respond(HttpStatusCode.Unauthorized)
            }

            val rawPayload = rawBody.parseUrlEncodedParameters()["payload"]
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
