
package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.domain.Location
import com.lunatech.chef.api.domain.NewLocation
import com.lunatech.chef.api.persistence.services.LocationsService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
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
import java.util.UUID
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class UpdatedLocation(val city: String, val country: String)

fun Routing.locations(locationsService: LocationsService) {
    val locationsRoute = "/locations"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(locationsRoute) {
            authenticate("session-auth") {
                // rolesAllowed(Role.ADMIN) {
                // get all locations
                get {
                    val locations = locationsService.getAll()
                    call.respond(OK, locations)
                }
                // create a new single location
                post {
                    try {
                        val newLocation = call.receive<NewLocation>()
                        val inserted = locationsService.insert(Location.fromNewLocation(newLocation))
                        if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
                    } catch (exception: Exception) {
                        logger.error("Error creating a new Location :( ", exception)
                        call.respond(HttpStatusCode.BadRequest, exception)
                    }
                }

                route(uuidRoute) {
                    // get single location
                    get {
                        val uuid = call.parameters[uuidParam]
                        val locations = locationsService.getByUuid(UUID.fromString(uuid))
                        if (locations.isEmpty()) {
                            call.respond(NotFound)
                        } else {
                            call.respond(OK, locations.first())
                        }
                    }
                    // modify existing location
                    put {
                        try {
                            val uuid = call.parameters[uuidParam]
                            val updatedLocation = call.receive<UpdatedLocation>()
                            val result = locationsService.update(UUID.fromString(uuid), updatedLocation)
                            if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                        } catch (exception: Exception) {
                            logger.error("Error updating a Location :( ", exception)
                            call.respond(HttpStatusCode.BadRequest, exception.message ?: "")
                        }
                    }
                    // delete a single location
                    delete {
                        val uuid = call.parameters[uuidParam]
                        val result = locationsService.delete(UUID.fromString(uuid))
                        if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                    }
                }
            // }
        }
    }
}
