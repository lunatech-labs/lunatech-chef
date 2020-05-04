package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.routes.UpdatedAttendance
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.dsl.where

class AttendancesService(val database: Database) {
    fun getAll() = database.from(Attendances).select().map { Attendances.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Attendance> =
        database.from(Attendances).select().where { -> Attendances.uuid eq uuid }.map { Attendances.createEntity(it) }

    fun insert(attendance: Attendance): Int =
        database.insert(Attendances) {
            it.uuid to attendance.uuid
            it.scheduleUuuid to attendance.scheduleUuid
            it.userUuid to attendance.userUuid
            it.isAttending to attendance.isAttending
        }

    fun update(uuid: UUID, attendance: UpdatedAttendance): Int =
        database.update(Attendances) {
            it.isAttending to attendance.isAttending
            where {
                it.uuid eq uuid
            }
        }
}
