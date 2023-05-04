package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.routes.UpdatedAttendance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import java.util.UUID

class AttendancesService(val database: Database, val usersService: UsersService) {
    fun insert(attendance: Attendance): Int =
        database.insert(Attendances) {
            set(it.uuid, attendance.uuid)
            set(it.scheduleUuid, attendance.scheduleUuid)
            set(it.userUuid, attendance.userUuid)
            set(it.isAttending, attendance.isAttending)
            set(it.isDeleted, attendance.isDeleted)
        }

    fun update(uuid: UUID, attendance: UpdatedAttendance): Int =
        database.update(Attendances) {
            set(it.isAttending, attendance.isAttending)
            where {
                it.uuid eq uuid
            }
        }

    fun insertAttendanceAllUsers(scheduleUuid: UUID, isAttending: Boolean?): Int = usersService.getAll().sumOf { user ->
        database.insert(Attendances) {
            set(it.uuid, UUID.randomUUID())
            set(it.scheduleUuid, scheduleUuid)
            set(it.userUuid, user.uuid)
            set(it.isAttending, isAttending)
            set(it.isDeleted, false)
        }
    }
}
