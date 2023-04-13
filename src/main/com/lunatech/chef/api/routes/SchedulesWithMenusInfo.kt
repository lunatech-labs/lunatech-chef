package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.SchedulesWithMenuInfoService
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.LocalDate
import java.util.UUID

fun Routing.schedulesWithMenusInfo(schedulesWithInfoService: SchedulesWithMenuInfoService) {
    val menusRoute = "/schedulesWithMenusInfo"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"
    val fromDateParam = "fromdate"
    val officeParam = "office"

    route(menusRoute) {
        authenticate("session-auth", "auth-jwt") {
            // rolesAllowed(Role.ADMIN) {
            // get all menus with the complete data about the dishes
            get {
                // check for filter parameters
                val maybeDateFrom = call.parameters[fromDateParam]
                val maybeOffice = call.parameters[officeParam]

                val dateFrom = if (maybeDateFrom != null) LocalDate.parse(maybeDateFrom) else null
                val officeName = if (maybeOffice != null) UUID.fromString(maybeOffice) else null

                val schedules = schedulesWithInfoService.getFiltered(dateFrom, officeName)

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
