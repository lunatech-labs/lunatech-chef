package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.persistence.services.SchedulesService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
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
        // get all schedules
        get {
            val schedule = schedulesService.getAll()
            call.respond(HttpStatusCode.OK, schedule)
        }
        // create a new single schedule
        post {
            val newSchedule = call.receive<Schedule>()
            val inserted = schedulesService.insert(newSchedule)
            if (inserted == 1) call.respond(HttpStatusCode.Created) else call.respond(HttpStatusCode.InternalServerError)
        }

        route(uuidRoute) {
            // get single schedule
            get {
                val uuid = call.parameters[uuidParam]
                val schedule = schedulesService.getByUuid(UUID.fromString(uuid))
                if (schedule.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(HttpStatusCode.OK, schedule.first())
                }
            }
            // modify existing schedule
            put {
                val uuid = call.parameters[uuidParam]
                val updatedSchedule = call.receive<UpdatedSchedule>()
                val result = schedulesService.update(UUID.fromString(uuid), updatedSchedule)
                if (result == 1) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
            }
            // delete a single schedule
            delete {
                val uuid = call.parameters[uuidParam]
                val result = schedulesService.delete(UUID.fromString(uuid))
                if (result == 1) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
