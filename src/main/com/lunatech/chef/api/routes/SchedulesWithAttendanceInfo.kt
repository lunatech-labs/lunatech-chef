package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.SchedulesWithAttendanceInfoService
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.LocalDate
import java.util.UUID

fun Routing.schedulesWithAttendanceInfo(schedulesWithAttendanceInfoService: SchedulesWithAttendanceInfoService) {
    val menusRoute = "/schedulesWithAttendanceInfo"
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

                val schedules = schedulesWithAttendanceInfoService.getFiltered(dateFrom, locationName)

                call.respond(OK, schedules)
            }
            // }
        }
    }
}
