package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Users
import com.lunatech.chef.api.routes.UpdatedAttendance
import java.util.UUID
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDate
import java.time.LocalDateTime

class AttendancesService(val database: Database, val usersService: UsersService) {
    fun getAll(): List<Attendance> =
        database.from(Attendances).select().where { Attendances.isDeleted eq false }
            .map { Attendances.createEntity(it) }


    fun getAllAttending(): List<Attendance> =
        database.from(Attendances).select().where { Attendances.isAttending }.map { Attendances.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Attendance> =
        database.from(Attendances).select().where { Attendances.uuid eq uuid }.map { Attendances.createEntity(it) }

    fun getByUser(userUuid: UUID): List<Attendance> =
        database.from(Attendances).select()
            .where { Attendances.userUuid eq userUuid and Attendances.isDeleted eq false }
            .map { Attendances.createEntity(it) }

    fun getByScheduleId(scheduleUuid: UUID): List<Attendance> =
        database.from(Attendances).select()
        .where { Attendances.scheduleUuid eq scheduleUuid and Attendances.isDeleted eq false }
        .map { Attendances.createEntity(it) }

    fun insert(attendance: Attendance): Int =
        database.insert(Attendances) {
            set(it.uuid, attendance.uuid)
            set(it.scheduleUuid, attendance.scheduleUuid)
            set(it.userUuid, attendance.userUuid)
            set(it.isAttending, attendance.isAttending)
            set(it.isDeleted, attendance.isDeleted)
            set(it.createdAt, attendance.createdAt)
            set(it.updatedAt, attendance.updatedAt)
        }

    fun update(uuid: UUID, attendance: UpdatedAttendance): Int =
        database.update(Attendances) {
            set(it.isAttending, attendance.isAttending)
            set(it.updatedAt, LocalDateTime.now())
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(Attendances) {
        set(it.isDeleted, true)
        where {
            it.uuid eq uuid
        }
    }

    fun insertAttendanceAllUsers(scheduleUuid: UUID, isAttending: Boolean): Int = usersService.getAll().map { user ->
        database.insert(Attendances) {
            set(it.uuid, UUID.randomUUID())
            set(it.scheduleUuid, scheduleUuid)
            set(it.userUuid, user.uuid)
            set(it.isAttending, isAttending)
            set(it.isDeleted, false)
        }
    }.sum()
}
