package com.lunatech.chef.api.schedulers.monthlyreports

import com.lunatech.chef.api.config.MailerConfig
import com.lunatech.chef.api.config.MonthlyReportConfig
import com.lunatech.chef.api.persistence.services.ExcelService
import com.lunatech.chef.api.persistence.services.ReportService
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.simplejavamail.api.email.Email
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

class MRJob() : Job {

    companion object {
        const val reportService: String = "reportService"
        const val excelService: String = "excelService"
        const val monthlyReportConfig: String = "monthlyReportConfig"
        const val mailerConfig: String = "mailerConfig"
    }

    override fun execute(context: JobExecutionContext?) {
        logger.info("Starting job that creates and sends a monthly report")

        val dataMap = context!!.jobDetail.jobDataMap

        val reportService: ReportService = dataMap[reportService] as ReportService
        val excelService: ExcelService = dataMap[excelService] as ExcelService
        val monthlyReportConfig: MonthlyReportConfig = dataMap[monthlyReportConfig] as MonthlyReportConfig
        val mailerConfig: MailerConfig = dataMap[mailerConfig] as MailerConfig

        val lastMonthDate = LocalDate.now().minusMonths(1)
        val year = lastMonthDate.year
        val month = lastMonthDate.month.value
        val monthName = lastMonthDate.month.name

        logger.info("building report for year $year")
        logger.info("building report for month $month $monthName")

        val reportEntries = reportService.getReportForDate(year, month)
        val excelReport = excelService.exportToExcel(reportEntries)

        val email: Email = EmailBuilder.startingBlank()
            .from(monthlyReportConfig.from)
            .to(monthlyReportConfig.to)
            .withSubject(monthlyReportConfig.subject)
            .withPlainText("Please find the lunch planner monthly report attached, for the month of $monthName")
            .withAttachment("report.xls", excelReport, "application/vnd.ms-excel")
            .buildEmail()

        val mailer: Mailer = MailerBuilder
            .withSMTPServer(
                mailerConfig.host,
                mailerConfig.port,
                mailerConfig.user,
                mailerConfig.password,
            )
            .buildMailer()

        mailer.sendMail(email)
        logger.info("Monthly report for the month of $monthName sent to ${monthlyReportConfig.to}")
    }
}
