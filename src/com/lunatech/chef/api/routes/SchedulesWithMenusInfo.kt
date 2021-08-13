package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.Role
import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.SchedulesWithInfoService
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

private val logger = KotlinLogging.logger {}
fun Routing.schedulesWithMenusInfo(schedulesWithInfoService: SchedulesWithInfoService) {
    val menusRoute = "/schedulesWithMenusInfo"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"
    val fromDateParam = "fromdate"
    val untilDateParam = "untildate"

    route(menusRoute) {
        authenticate("session-auth") {
            rolesAllowed(Role.ADMIN) {
                // get all menus with the complete data about the dishes
                get {
                    // check for filter parameters
                    val dateFrom = call.parameters[fromDateParam]
                    val dateUntil = call.parameters[untilDateParam]

                    val schedules =
                        if (dateFrom != null && dateUntil == null) {
                            schedulesWithInfoService.getFilterFromDate(LocalDate.parse(dateFrom))
                        } else if (dateFrom != null && dateUntil != null) {
                            schedulesWithInfoService.getFilterFromDateUntilDate(
                                LocalDate.parse(dateFrom),
                                LocalDate.parse(dateUntil)
                            )
                        } else {
                            schedulesWithInfoService.getAll()
                        }
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
            }
        }
    }
}
