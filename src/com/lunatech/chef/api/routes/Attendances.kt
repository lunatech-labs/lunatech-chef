package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.Role
// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.domain.NewAttendance
import com.lunatech.chef.api.persistence.services.AttendancesService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import java.util.UUID
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class UpdatedAttendance(val isAttending: Boolean)

fun Routing.attendances(attendancesService: AttendancesService) {
    val attendancesRoute = "/attendances"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(attendancesRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN, Role.USER) {
                // create a new single attendance
                post {
                    try {
                        val newAttendance = call.receive<NewAttendance>()
                        val inserted = attendancesService.insert(Attendance.fromNewAttendance(newAttendance))
                        if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
                    } catch (exception: Exception) {
                        logger.error("Error creating a new Attendance :( ", exception)
                        call.respond(BadRequest, exception.message ?: "")
                    }
                }
                route(uuidRoute) {
                    // modify existing schedule
                    put {
                        try {
                            val uuid = call.parameters[uuidParam]
                            val updatedAttendance = call.receive<UpdatedAttendance>()
                            val result = attendancesService.update(UUID.fromString(uuid), updatedAttendance)
                            if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                        } catch (exception: Exception) {
                            logger.error("Error updating an attendance :( ", exception)
                            call.respond(BadRequest, exception.message ?: "")
                        }
                    }
                }
            // }
        }
    }
}
