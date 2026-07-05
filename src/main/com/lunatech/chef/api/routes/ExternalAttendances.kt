package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.services.ExternalAttendancesService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class UpdatedExternalAttendance(
    val attendancesCount: Int,
)

fun Route.externalAttendances(externalAttendancesService: ExternalAttendancesService) {
    val externalAttendancesRoute = "/externalAttendances"

    route(externalAttendancesRoute) {
        route(UUID_ROUTE) {
            // modify existing external attendance
            put {
                try {
                    val uuid =
                        call.parameters[UUID_PARAM].toUUIDOrNull() ?: return@put call.respond(
                            BadRequest,
                            "Invalid UUID",
                        )
                    val updatedExternalAttendance = call.receive<UpdatedExternalAttendance>()
                    val result = externalAttendancesService.update(uuid, updatedExternalAttendance)
                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                } catch (exception: Exception) {
                    logger.error("Error updating an external attendance :( ", exception)
                    call.respond(BadRequest, exception.message ?: "")
                }
            }
        }
    }
}
