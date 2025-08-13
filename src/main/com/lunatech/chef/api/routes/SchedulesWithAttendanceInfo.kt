package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.services.SchedulesWithAttendanceInfoService
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.LocalDate
import java.util.UUID

fun Route.schedulesWithAttendanceInfo(schedulesWithAttendanceInfoService: SchedulesWithAttendanceInfoService) {
    val menusRoute = "/schedulesWithAttendanceInfo"
    val fromDateParam = "fromdate"
    val officeParam = "office"

    route(menusRoute) {
        // get all menus with the complete data about the dishes
        get {
            // check for filter parameters
            val maybeDateFrom = call.parameters[fromDateParam]
            val maybeOffice = call.parameters[officeParam]

            val dateFrom = if (maybeDateFrom != null) LocalDate.parse(maybeDateFrom) else null
            val officeName = if (maybeOffice != null) UUID.fromString(maybeOffice) else null

            val schedules = schedulesWithAttendanceInfoService.getFiltered(dateFrom, officeName)

            call.respond(OK, schedules)
        }
    }
}
