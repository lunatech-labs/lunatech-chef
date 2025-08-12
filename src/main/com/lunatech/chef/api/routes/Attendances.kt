package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.domain.NewAttendance
import com.lunatech.chef.api.persistence.services.AttendancesService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

data class UpdatedAttendance(val isAttending: Boolean)

fun Route.attendances(attendancesService: AttendancesService) {
    val attendancesRoute = "/attendances"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(attendancesRoute) {
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
    }
}
