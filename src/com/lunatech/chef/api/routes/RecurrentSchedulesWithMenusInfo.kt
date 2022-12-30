package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.RecurrentSchedulesWithMenuInfo
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

fun Routing.recurrentSchedulesWithMenusInfo(recurrentSchedulesWithInfoService: RecurrentSchedulesWithMenuInfo) {
    val menusRoute = "/recurrentSchedulesWithMenusInfo"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"
    val locationParam = "location"

    route(menusRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
            // get all menus with the complete data about the dishes
            get {
                // check for filter parameters
                val maybeLocation = call.parameters[locationParam]
                val locationName = if (maybeLocation != null) UUID.fromString(maybeLocation) else null

                val schedules = recurrentSchedulesWithInfoService.getFiltered(locationName)

                call.respond(OK, schedules)
            }
            route(uuidRoute) {
                // get single menu with the complete data about the dishes
                get {
                    val uuid = call.parameters[uuidParam]!!
                    val schedules = recurrentSchedulesWithInfoService.getByUuid(UUID.fromString(uuid))

                    if (schedules.isEmpty()) {
                        call.respond(NotFound)
                    } else {
                        call.respond(OK, schedules.first())
                    }
                }
            }
            // }
        }
    }
}
