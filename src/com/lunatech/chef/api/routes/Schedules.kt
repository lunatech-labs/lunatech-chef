package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.Role
// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.domain.NewSchedule
import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.BadRequest
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
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class UpdatedSchedule(val menuUuid: UUID, val date: LocalDate, val locationUuid: UUID)

fun Routing.schedules(schedulesService: SchedulesService, attendancesService: AttendancesService) {
    val schedulesRoute = "/schedules"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"
    val error = -1

    route(schedulesRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
                // get all schedules
                get {
                    val schedules = schedulesService.getAll()
                    call.respond(OK, schedules)
                }
                // create a new single schedule
                post {
                    try {
                        val newSchedule = call.receive<NewSchedule>()
                        val scheduleToInsert = Schedule.fromNewSchedule(newSchedule)
                        val insertedSchedule = schedulesService.insert(scheduleToInsert)

                        val insertedAttendance = if (insertedSchedule == 1) attendancesService.insertAttendanceAllUsers(
                            scheduleToInsert.uuid,
                            isAttending = false
                        ) else error
                        if (insertedAttendance > 0) call.respond(Created) else call.respond(InternalServerError)
                    } catch (exception: Exception) {
                        logger.error("Error adding new Schedule :( ", exception)
                        call.respond(BadRequest, exception.message ?: "")
                    }
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
                        try {
                            val uuid = call.parameters[uuidParam]
                            val updatedSchedule = call.receive<UpdatedSchedule>()
                            val result = schedulesService.update(UUID.fromString(uuid), updatedSchedule)
                            if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                        } catch (exception: Exception) {
                            logger.error("Error updating a Schedule :( ", exception)
                            call.respond(BadRequest, exception.message ?: "")
                        }
                    }
                    // delete a single schedule
                    delete {
                        val uuid = call.parameters[uuidParam]
                        val result = schedulesService.delete(UUID.fromString(uuid))

                        // TODO delete all related attendance

                        if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                    }
                }
            // }
        }
    }
}
