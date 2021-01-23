package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.domain.AttendanceWithInfo
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Locations
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.where

class AttendancesWithInfoService(
    val database: Database,
    private val schedulesService: SchedulesService,
    private val menusWithDishesService: MenusWithDishesNamesService
) {
    fun getByUserUuid(userUuid: UUID): List<AttendanceWithInfo> =
        database.from(Attendances).select()
            .where { (Attendances.userUuid eq userUuid) and (Attendances.isDeleted eq false) }
            .map { Attendances.createEntity(it) }
            .flatMap { getAttendanceWithInfo(it) }

    private fun getAttendanceWithInfo(attendance: Attendance): List<AttendanceWithInfo> {
        return schedulesService.getByUuid(attendance.scheduleUuid).flatMap { schedule ->
            val menu = menusWithDishesService.getByUuid(schedule.menuUuid)
            database.from(Locations).select()
                .where { Locations.uuid eq schedule.locationUuid }
                .map { Locations.createEntity(it) }
                .map { location ->
                    AttendanceWithInfo(
                        uuid = attendance.uuid,
                        userUuid = attendance.userUuid,
                        scheduleUuid = schedule.uuid,
                        menu = menu!!,
                        date = schedule.date,
                        location = location,
                        isAttending = attendance.isAttending
                    )
                }
        }
    }
}
