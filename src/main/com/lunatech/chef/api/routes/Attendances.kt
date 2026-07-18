package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.mayManageUser
import com.lunatech.chef.api.auth.respondForbidden
import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.domain.NewAttendance
import com.lunatech.chef.api.persistence.services.AttendancesService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class UpdatedAttendance(
    val isAttending: Boolean,
)

fun Route.attendances(attendancesService: AttendancesService) {
    val attendancesRoute = "/attendances"

    route(attendancesRoute) {
        // create a new single attendance, own attendance or admin
        post {
            try {
                val newAttendance = call.receive<NewAttendance>()
                if (!call.mayManageUser(newAttendance.userUuid)) return@post call.respondForbidden()
                val inserted = attendancesService.insert(Attendance.fromNewAttendance(newAttendance))
                if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
            } catch (exception: Exception) {
                logger.error("Error creating a new Attendance :( ", exception)
                call.respond(BadRequest, exception.message ?: "")
            }
        }
        route(UUID_ROUTE) {
            // modify existing attendance, own attendance or admin
            put {
                try {
                    val uuid = call.parameters[UUID_PARAM].toUUIDOrNull() ?: return@put call.respond(BadRequest, "Invalid UUID")
                    val attendance = attendancesService.getByUuid(uuid) ?: return@put call.respond(NotFound)
                    if (!call.mayManageUser(attendance.userUuid)) return@put call.respondForbidden()
                    val updatedAttendance = call.receive<UpdatedAttendance>()
                    val result = attendancesService.update(uuid, updatedAttendance)
                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                } catch (exception: Exception) {
                    logger.error("Error updating an attendance :( ", exception)
                    call.respond(BadRequest, exception.message ?: "")
                }
            }
        }
    }
}
