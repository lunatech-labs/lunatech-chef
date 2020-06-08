package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.NewSchedule
import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.persistence.services.SchedulesService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import java.time.LocalDate
import java.util.UUID

data class UpdatedSchedule(val menuUuid: UUID, val date: LocalDate, val location: UUID)

fun Routing.schedules(schedulesService: SchedulesService) {
    val schedulesRoute = "/schedules"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(schedulesRoute) {
        authenticate("session-auth") {
            // get all schedules
            get {
                val schedules = schedulesService.getAll()
                call.respond(OK, schedules)
            }
            // create a new single schedule
            post {
                val newSchedule = call.receive<NewSchedule>()
                val inserted = schedulesService.insert(Schedule.fromNewSchedule(newSchedule))
                if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
            }

            route(uuidRoute) {
                // get single schedule
                get {
                    val uuid = call.parameters[uuidParam]
                    val schedule = schedulesService.getByUuid(UUID.fromString(uuid))
                    if (schedule.isEmpty()) {
                        call.respond(NotFound)
                    } else {
                        call.respond(OK, schedule.first())
                    }
                }
                // modify existing schedule
                put {
                    val uuid = call.parameters[uuidParam]
                    val updatedSchedule = call.receive<UpdatedSchedule>()
                    val result = schedulesService.update(UUID.fromString(uuid), updatedSchedule)
                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                }
                // delete a single schedule
                delete {
                    val uuid = call.parameters[uuidParam]
                    val result = schedulesService.delete(UUID.fromString(uuid))
                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                }
            }
        }
    }
}
