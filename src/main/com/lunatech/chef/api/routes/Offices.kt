package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.NewOffice
import com.lunatech.chef.api.domain.Office
import com.lunatech.chef.api.persistence.services.OfficesService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

data class UpdatedOffice(val city: String, val country: String)

fun Route.offices(officesService: OfficesService) {
    val officesRoute = "/offices"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(officesRoute) {
        // get all offices
        get {
            val offices = officesService.getAll()
            call.respond(OK, offices)
        }
        // create a new single office
        post {
            try {
                val newOffice = call.receive<NewOffice>()
                val inserted = officesService.insert(Office.fromNewOffice(newOffice))
                if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
            } catch (exception: Exception) {
                logger.error("Error creating a new Office :( ", exception)
                call.respond(BadRequest, exception)
            }
        }

        route(uuidRoute) {
            // get single office
            get {
                val uuid = call.parameters[uuidParam]
                val offices = officesService.getByUuid(UUID.fromString(uuid))
                if (offices.isEmpty()) {
                    call.respond(NotFound)
                } else {
                    call.respond(OK, offices.first())
                }
            }
            // modify existing office
            put {
                try {
                    val uuid = call.parameters[uuidParam]
                    val updatedOffice = call.receive<UpdatedOffice>()
                    val result = officesService.update(UUID.fromString(uuid), updatedOffice)
                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                } catch (exception: Exception) {
                    logger.error("Error updating an office :( ", exception)
                    call.respond(BadRequest, exception.message ?: "")
                }
            }
            // delete a single office
            delete {
                val uuid = call.parameters[uuidParam]
                val result = officesService.delete(UUID.fromString(uuid))
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
        }
    }
}
