package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.SchedulesWithMenuInfo
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import java.time.LocalDate
import java.util.UUID
import mu.KotlinLogging

fun Routing.schedulesWithMenusInfo(schedulesWithInfoService: SchedulesWithMenuInfo) {
    val menusRoute = "/schedulesWithMenusInfo"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"
    val fromDateParam = "fromdate"
    val locationParam = "location"

    route(menusRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
                // get all menus with the complete data about the dishes
                get {
                    // check for filter parameters
                    val maybeDateFrom = call.parameters[fromDateParam]
                    val maybeLocation = call.parameters[locationParam]

                    val dateFrom = if (maybeDateFrom != null) LocalDate.parse(maybeDateFrom) else null
                    val locationName = if (maybeLocation != null) UUID.fromString(maybeLocation) else null

                    val schedules = schedulesWithInfoService.getFiltered(dateFrom, locationName)

                    call.respond(OK, schedules)
                }
                route(uuidRoute) {
                    // get single menu with the complete data about the dishes
                    get {
                        val uuid = call.parameters[uuidParam]!!
                        val schedules = schedulesWithInfoService.getByUuid(UUID.fromString(uuid))

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
