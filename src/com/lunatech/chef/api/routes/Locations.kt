
package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.Location
import com.lunatech.chef.api.persistence.services.LocationsService
import io.ktor.application.call
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

data class UpdatedLocation(val city: String, val country: String)

fun Routing.locations(locationsService: LocationsService) {
    val locationsRoute = "/locations"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(locationsRoute) {
        // get all locations
        get {
            val locations = locationsService.getAll()
            call.respond(OK, locations)
        }
        // create a new single location
        post {
            val newLocation = call.receive<Location>()
            val inserted = locationsService.insert(newLocation)
            if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
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
                val uuid = call.parameters[uuidParam]
                val updatedLocation = call.receive<UpdatedLocation>()
                val result = locationsService.update(UUID.fromString(uuid), updatedLocation)
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
            // delete a single location
            delete {
                val uuid = call.parameters[uuidParam]
                val result = locationsService.delete(UUID.fromString(uuid))
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
        }
    }
}
