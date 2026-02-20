package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.services.AttendancesForSlackbotService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import mu.KotlinLogging
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

fun Route.attendancesForSlackbot(attendancesForSlackbotService: AttendancesForSlackbotService) {
    val attendancesRoute = "/attendancesforslackbot"
    val fromDateParam = "fromdate"
    val untilDateParam = "untildate"

    route(attendancesRoute) {
        get {
            val maybeDateFrom = call.parameters[fromDateParam]
            val maybeDateUntil = call.parameters[untilDateParam]

            val dateFrom = if (maybeDateFrom != null) LocalDate.parse(maybeDateFrom) else null
            val dateUntil = if (maybeDateUntil != null) LocalDate.parse(maybeDateUntil) else null

            if (dateParametersAreValid(dateFrom, dateUntil)) {
                call.respond(
                    OK,
                    attendancesForSlackbotService.getMissingAttendances(dateFrom!!, dateUntil!!),
                )
            } else {
                call.respond(
                    BadRequest,
                    "Parameters `fromdate` and `untildate` are mandatory and `untildate` must be bigger or equal to `fromdate`",
                )
            }
        }
    }
}

fun dateParametersAreValid(
    dateFrom: LocalDate?,
    dateUntil: LocalDate?,
): Boolean = (dateFrom != null && dateUntil != null) && (dateUntil.isAfter(dateFrom) || dateUntil.isEqual(dateFrom))
