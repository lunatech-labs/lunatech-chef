package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.services.AttendancesWithScheduleInfoService
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.util.*

fun Route.attendancesWithScheduleInfo(attendancesWithScheduleInfoService: AttendancesWithScheduleInfoService) {
    val menusRoute = "/attendancesWithScheduleInfo"
    val userUuidRoute = "/{useruuid}"
    val userUuidParam = "useruuid"
    val fromDateParam = "fromdate"
    val officeParam = "office"

    route(menusRoute) {
        route(userUuidRoute) {
            // get all attendances for a user with the data about the menus
            get {
                val uuid = call.parameters[userUuidParam]

                // check for filter parameters
                val maybeDateFrom = call.parameters[fromDateParam]
                val maybeOffice = call.parameters[officeParam]

                val dateFrom = if (maybeDateFrom != null) LocalDate.parse(maybeDateFrom) else null
                val officeName = if (maybeOffice != null) UUID.fromString(maybeOffice) else null

                val attendance =
                    attendancesWithScheduleInfoService.getByUserUuidFiltered(
                        UUID.fromString(uuid),
                        dateFrom,
                        officeName,
                    )
                call.respond(OK, attendance)
            }
        }
    }
}
