package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.AttendancesWithScheduleInfoService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import java.util.UUID

fun Routing.attendancesWithScheduleInfo(attendancesWithScheduleInfoService: AttendancesWithScheduleInfoService) {
    val menusRoute = "/attendancesWithScheduleInfo"
    val userUuidRoute = "/{useruuid}"
    val userUuidParam = "useruuid"

    route(menusRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
                route(userUuidRoute) {
                    // get all attendances for a user with the data about the menus
                    get {
                        val uuid = call.parameters[userUuidParam]
                        val attendance = attendancesWithScheduleInfoService.getByUserUuid(UUID.fromString(uuid))

                        if (attendance.isEmpty()) {
                            call.respond(NotFound)
                        } else {
                            call.respond(OK, attendance)
                        }
                    }
                }
            // }
        }
    }
}
