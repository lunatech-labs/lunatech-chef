package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.AttendancesWithScheduleInfoService
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.LocalDate
import java.util.UUID

fun Routing.attendancesWithScheduleInfo(attendancesWithScheduleInfoService: AttendancesWithScheduleInfoService) {
    val menusRoute = "/attendancesWithScheduleInfo"
    val userUuidRoute = "/{useruuid}"
    val userUuidParam = "useruuid"
    val fromDateParam = "fromdate"
    val locationParam = "location"

    route(menusRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
            route(userUuidRoute) {
                // get all attendances for a user with the data about the menus
                get {
                    val uuid = call.parameters[userUuidParam]

                    // check for filter parameters
                    val maybeDateFrom = call.parameters[fromDateParam]
                    val maybeLocation = call.parameters[locationParam]

                    val dateFrom = if (maybeDateFrom != null) LocalDate.parse(maybeDateFrom) else null
                    val locationName = if (maybeLocation != null) UUID.fromString(maybeLocation) else null

                    val attendance = attendancesWithScheduleInfoService.getByUserUuidFiltered(
                        UUID.fromString(uuid),
                        dateFrom,
                        locationName
                    )
                    call.respond(OK, attendance)
                }
            }
            // }
        }
    }
}
