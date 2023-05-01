package com.lunatech.chef.api.domain

import java.time.LocalDate
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
    val isAttending: Boolean?,
    val isDeleted: Boolean = false,
) {
    companion object {
        fun fromNewAttendance(newAttendance: NewAttendance): Attendance {
            return Attendance(
                uuid = UUID.randomUUID(),
                scheduleUuid = newAttendance.scheduleUuid,
                userUuid = newAttendance.userUuid,
                isAttending = newAttendance.isAttending,
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
    val office: Office,
    val isAttending: Boolean,
)

data class AttendanceForSlackbot(
    val attendanceUuid: UUID,
    val emailAddress: String,
    val date: LocalDate,
    val office: String,
    val menuName: String,
)
