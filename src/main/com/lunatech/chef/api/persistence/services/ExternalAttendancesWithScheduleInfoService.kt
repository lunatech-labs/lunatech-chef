package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.ExternalAttendance
import com.lunatech.chef.api.domain.ExternalAttendanceWithInfo
import com.lunatech.chef.api.persistence.schemas.ExternalAttendances
import com.lunatech.chef.api.persistence.schemas.Offices
import com.lunatech.chef.api.persistence.schemas.Schedules
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate
import java.util.UUID

class ExternalAttendancesWithScheduleInfoService(
    val database: Database,
    private val schedulesService: SchedulesService,
    private val menusWithDishesService: MenusWithDishesNamesService,
) {
    fun getAllFromDateAndOffice(
        fromDate: LocalDate?,
        office: UUID?,
    ): List<ExternalAttendanceWithInfo> =
        database
            .from(ExternalAttendances)
            .leftJoin(Schedules, on = Schedules.uuid eq ExternalAttendances.scheduleUuid)
            .select()
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()
                conditions += ExternalAttendances.isDeleted eq false

                if (fromDate != null) {
                    conditions += Schedules.date greaterEq fromDate
                }
                if (office != null) {
                    conditions += Schedules.officeUuid eq office
                }

                conditions.reduce { a, b -> a and b }
            }.orderBy(Schedules.date.asc())
            .map { ExternalAttendances.createEntity(it) }
            .flatMap { getExternalAttendanceWithInfo(it) }

    private fun getExternalAttendanceWithInfo(externalAttendance: ExternalAttendance): List<ExternalAttendanceWithInfo> =
        schedulesService.getByUuid(externalAttendance.scheduleUuid).flatMap { schedule ->
            val menu = menusWithDishesService.getByUuid(schedule.menuUuid)
            database
                .from(Offices)
                .select()
                .where { Offices.uuid eq schedule.officeUuid }
                .map { Offices.createEntity(it) }
                .map { office ->
                    ExternalAttendanceWithInfo(
                        uuid = externalAttendance.uuid,
                        scheduleUuid = schedule.uuid,
                        menu = menu!!,
                        date = schedule.date,
                        office = office.city,
                        attendancesCount = externalAttendance.attendancesCount,
                    )
                }
        }
}
