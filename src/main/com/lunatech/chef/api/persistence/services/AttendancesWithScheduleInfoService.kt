package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.domain.AttendanceWithInfo
import com.lunatech.chef.api.persistence.schemas.Attendances
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

class AttendancesWithScheduleInfoService(
    val database: Database,
    private val schedulesService: SchedulesService,
    private val menusWithDishesService: MenusWithDishesNamesService,
) {
    fun getByUserUuidFiltered(userUuid: UUID, fromDate: LocalDate?, office: UUID?): List<AttendanceWithInfo> =
        database.from(Attendances)
            .leftJoin(Schedules, on = Schedules.uuid eq Attendances.scheduleUuid)
            .select()
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()

                conditions += Attendances.userUuid eq userUuid
                conditions += Attendances.isDeleted eq false

                if (fromDate != null) {
                    conditions += Schedules.date greaterEq fromDate
                }
                if (office != null) {
                    conditions += Schedules.officeUuid eq office
                }

                conditions.reduce { a, b -> a and b }
            }
            .orderBy(Schedules.date.asc())
            .map { Attendances.createEntity(it) }
            .flatMap { getAttendanceWithInfo(it) }

    private fun getAttendanceWithInfo(attendance: Attendance): List<AttendanceWithInfo> {
        return schedulesService.getByUuid(attendance.scheduleUuid).flatMap { schedule ->
            val menu = menusWithDishesService.getByUuid(schedule.menuUuid)
            database.from(Offices).select()
                .where { Offices.uuid eq schedule.officeUuid }
                .map { Offices.createEntity(it) }
                .map { office ->
                    AttendanceWithInfo(
                        uuid = attendance.uuid,
                        userUuid = attendance.userUuid,
                        scheduleUuid = schedule.uuid,
                        menu = menu!!,
                        date = schedule.date,
                        office = office,
                        isAttending = attendance.isAttending ?: false,
                    )
                }
        }
    }
}
