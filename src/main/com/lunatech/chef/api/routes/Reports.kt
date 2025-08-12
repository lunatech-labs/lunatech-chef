package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.services.ExcelService
import com.lunatech.chef.api.persistence.services.ReportService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

fun Route.reports(
    reportService: ReportService,
    excelService: ExcelService,
) {
    val reportsRoute = "/reports"
    val yearParam = "year"
    val monthParam = "month"

    route(reportsRoute) {
        get {
            try {
                val year = call.parameters[yearParam]?.toInt()
                val month = call.parameters[monthParam]?.toInt()

                if (year == null || month == null) {
                    call.respond(HttpStatusCode.BadRequest, "Both year and month parameters are mandatory!")
                } else {
                    val reportEntries = reportService.getReportByMonth(year, month)
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
