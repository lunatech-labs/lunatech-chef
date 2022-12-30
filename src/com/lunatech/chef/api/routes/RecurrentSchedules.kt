package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.domain.NewRecurrentSchedule
import com.lunatech.chef.api.domain.RecurrentSchedule
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.time.LocalDate
import java.util.UUID
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class UpdatedRecurrentSchedule(
  val menuUuid: UUID,
  val locationUuid: UUID,
  val repetitionDays: Int,
  val nextDate: LocalDate
)

fun Routing.recurrentSchedules(recurrentSchedulesService: RecurrentSchedulesService) {
    val recurrentSchedulesRoute = "/recurrentschedules"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(recurrentSchedulesRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
            // get all recurrent schedules
            get {
                val schedules = recurrentSchedulesService.getAll()
                call.respond(OK, schedules)
            }
            post {
                try {
                    val newRecurrentSchedule = call.receive<NewRecurrentSchedule>()
                    val recurrentScheduleToInsert = RecurrentSchedule.fromNewRecurrentSchedule(newRecurrentSchedule)
                    val inserted = recurrentSchedulesService.insert(recurrentScheduleToInsert)
                    if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
                } catch (exception: Exception) {
                    logger.error("Error adding new RecurrentSchedule :( ", exception)
                    call.respond(BadRequest, exception.message ?: "")
                }
            }
            route(uuidRoute) {
                // get single recurrent schedule
                get {
                    val uuid = call.parameters[uuidParam]
                    val schedule = recurrentSchedulesService.getByUuid(UUID.fromString(uuid))
                    if (schedule.isEmpty()) {
                        call.respond(NotFound)
                    } else {
                        call.respond(OK, schedule.first())
                    }
                }
                // modify an existing recurrent schedule
                put {
                    try {
                        val uuid = call.parameters[uuidParam]
                        val updatedRecurrentSchedule = call.receive<UpdatedRecurrentSchedule>()
                        val result = recurrentSchedulesService.update(UUID.fromString(uuid), updatedRecurrentSchedule)
                        if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                    } catch (exception: Exception) {
                        logger.error("Error updating a RecurrentSchedule :( ", exception)
                        call.respond(BadRequest, exception.message ?: "")
                    }
                }
                // delete a single recurrent schedule
                delete {
                    val uuid = call.parameters[uuidParam]
                    val result = recurrentSchedulesService.delete(UUID.fromString(uuid))

                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                }
            }
            // }
        }
    }
}
