package com.lunatech.chef.api.schedulers.monthlyreports

import com.lunatech.chef.api.config.MailerConfig
import com.lunatech.chef.api.config.MonthlyReportConfig
import com.lunatech.chef.api.persistence.services.ExcelService
import com.lunatech.chef.api.persistence.services.ReportService
import org.quartz.CronScheduleBuilder
import org.quartz.CronTrigger
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder

fun mrSchedulerTrigger(
    scheduler: Scheduler,
    cronExpression: String,
    monthlyReportConfig: MonthlyReportConfig,
    mailerConfig: MailerConfig,
    reportService: ReportService,
    excelService: ExcelService,
) {
    val job: JobDetail =
        JobBuilder.newJob(MRJob::class.java)
            .withIdentity("monthlyReports", "chefSchedules")
            .build()

    job.jobDataMap[MRJob.REPORT_SERVICE] = reportService
    job.jobDataMap[MRJob.EXCEL_SERVICE] = excelService
    job.jobDataMap[MRJob.MONTHLY_REPORT_CONFIG] = monthlyReportConfig
    job.jobDataMap[MRJob.MAILER_CONFIG] = mailerConfig

    val trigger: CronTrigger =
        TriggerBuilder.newTrigger()
            .withIdentity("monthlyReports", "monthlyReportsTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
            .build()
    scheduler.scheduleJob(job, trigger)
}
