package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.services.ExcelService
import com.lunatech.chef.api.persistence.services.ReportService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.Base64

private val logger = KotlinLogging.logger {}

fun Routing.reports(reportService: ReportService, excelService: ExcelService) {
    val reportsRoute = "/reports"
    val yearParam = "year"
    val monthParam = "month"

    route(reportsRoute) {
        authenticate("session-auth") {
            get {
                try {
                    val year = call.parameters[yearParam]?.toInt()
                    val month = call.parameters[monthParam]?.toInt()

                    if (year == null || month == null) {
                        call.respond(HttpStatusCode.BadRequest, "Both year and month parameters are mandatory!")
                    } else {
                        val reportEntries = reportService.getReportForDate(year, month)
                        val excelReport = excelService.exportToExcel(reportEntries)
                        val encodedReport = Base64.getEncoder().encode(excelReport)

                        call.respondOutputStream(
                            ContentType.Application.Xlsx,
                            HttpStatusCode.OK,
                        ) {
                            withContext(Dispatchers.IO) {
                                write(encodedReport)
                            }
                        }
                    }
                } catch (exception: Exception) {
                    logger.error("Error getting report ", exception)
                    call.respond(HttpStatusCode.BadRequest, exception.message ?: "")
                }
            }
        }
    }
}
