package com.lunatech.chef.api.domain

import org.ktorm.schema.datetime
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class NewAttendance(
    val scheduleUuid: UUID,
    val userUuid: UUID,
    val isAttending: Boolean,
)

data class Attendance(
    val uuid: UUID,
    val scheduleUuid: UUID,
    val userUuid: UUID,
    val isAttending: Boolean,
    val isDeleted: Boolean = false,
    val createdAt:LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromNewAttendance(newAttendance: NewAttendance): Attendance {
            return Attendance(
                uuid = UUID.randomUUID(),
                scheduleUuid = newAttendance.scheduleUuid,
                userUuid = newAttendance.userUuid,
                isAttending = newAttendance.isAttending,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        }
    }
}

data class AttendanceWithInfo(
    val uuid: UUID,
    val userUuid: UUID,
    val scheduleUuid: UUID,
    val menu: MenuWithDishes,
    val date: LocalDate,
    val location: Location,
    val isAttending: Boolean,
)
