package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.ReportEntry
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Offices
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.persistence.schemas.Users
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.greater
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.less
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.selectDistinct
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate

class ReportService(val database: Database) {

    fun getReportByMonth(year: Int, month: Int): List<ReportEntry> {
        val (startDate, endDate) = getTimeInterval(year, month)
        return database
            .from(Attendances)
            .leftJoin(Users, on = Attendances.userUuid eq Users.uuid)
            .leftJoin(Schedules, on = Attendances.scheduleUuid eq Schedules.uuid)
            .leftJoin(Offices, on = Schedules.officeUuid eq Offices.uuid)
            .selectDistinct(Schedules.date, Users.name, Offices.city, Offices.country)
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()
                conditions += Schedules.date greater startDate
                conditions += Schedules.date less endDate
                conditions += Attendances.isDeleted eq false
                conditions += Attendances.isAttending eq true
                conditions.reduce { a, b -> a and b }
            }
            .orderBy(Schedules.date.asc(), Users.name.asc(), Offices.city.asc(), Offices.country.asc())
            .map { row ->
                ReportEntry(
                    row[Schedules.date] ?: LocalDate.now(),
                    row[Users.name] ?: "",
                    row[Offices.city] ?: "",
                    row[Offices.country] ?: "",
                )
            }
    }

    private fun getTimeInterval(year: Int, month: Int): Pair<LocalDate, LocalDate> {
        val baseDate = LocalDate.now().withMonth(month).withYear(year)
        val startDate = baseDate.withDayOfMonth(1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        return Pair(startDate, endDate)
    }
}
