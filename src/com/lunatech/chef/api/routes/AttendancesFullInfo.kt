package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.Role
import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.AttendancesWithInfoService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import java.util.UUID

fun Routing.attendancesFullInfo(attendancesWithInfoService: AttendancesWithInfoService) {
    val menusRoute = "/attendancesFullInfo"
    val userUuidRoute = "/{useruuid}"
    val userUuidParam = "useruuid"

    route(menusRoute) {
        authenticate("session-auth") {
            rolesAllowed(Role.ADMIN) {
                route(userUuidRoute) {
                    // get all attendances for a user with the data about the menus
                    get {
                        val uuid = call.parameters[userUuidParam]
                        val attendance = attendancesWithInfoService.getByUserUuid(UUID.fromString(uuid))

                        if (attendance.isEmpty()) {
                            call.respond(NotFound)
                        } else {
                            call.respond(OK, attendance)
                        }
                    }
                }
            }
        }
    }
}
