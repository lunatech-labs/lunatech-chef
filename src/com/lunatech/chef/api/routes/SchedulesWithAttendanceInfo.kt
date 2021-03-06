package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.Role
import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.SchedulesWithDishesInfoService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route

fun Routing.schedulesWithAttendanceInfo(schedulesWithDishesInfoService: SchedulesWithDishesInfoService) {
    val menusRoute = "/schedulesWithAttendanceInfo"

    route(menusRoute) {
        authenticate("session-auth") {
            rolesAllowed(Role.ADMIN) {
                // get all menus with the complete data about the dishes
                get {
                    val schedules = schedulesWithDishesInfoService.getAllSchedulesWithAttendanceInfo()

                    if (schedules.isEmpty()) {
                        call.respond(NotFound)
                    } else {
                        call.respond(OK, schedules)
                    }
                }
            }
        }
    }
}
