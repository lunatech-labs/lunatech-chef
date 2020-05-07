package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.domain.NewAttendance
import com.lunatech.chef.api.persistence.services.AttendancesService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import java.util.UUID

data class UpdatedAttendance(val isAttending: Boolean)

fun Routing.attendances(attendancesService: AttendancesService) {
    val attendancesRoute = "/attendances"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(attendancesRoute) {
        // get all attendances // TODO filters
        get {
            val attendances = attendancesService.getAll()
            call.respond(OK, attendances)
        }
        // create a new single attendance
        post {
            val newAttendance = call.receive<NewAttendance>()
            val inserted = attendancesService.insert(Attendance.fromNewAttendance(newAttendance))
            if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
        }
        route(uuidRoute) {
            // get single attendance
            get {
                val uuid = call.parameters[uuidParam]
                val attendance = attendancesService.getByUuid(UUID.fromString(uuid))
                if (attendance.isEmpty()) {
                    call.respond(NotFound)
                } else {
                    call.respond(OK, attendance.first())
                }
            }
            // modify existing schedule
            put {
                val uuid = call.parameters[uuidParam]
                val updatedAttendance = call.receive<UpdatedAttendance>()
                val result = attendancesService.update(UUID.fromString(uuid), updatedAttendance)
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
        }
    }
}
