package com.lunatech.chef.api.domain

import java.util.UUID

data class NewAttendance(
  val scheduleUuid: UUID,
  val userUuid: UUID,
  val isAttending: Boolean
)

data class Attendance(
  val uuid: UUID,
  val scheduleUuid: UUID,
  val userUuid: UUID,
  val isAttending: Boolean
) {
  companion object {
    fun fromNewAttendance(newAttendance: NewAttendance): Attendance {
      return Attendance(
        uuid = UUID.randomUUID(),
        scheduleUuid = newAttendance.scheduleUuid,
        userUuid = newAttendance.userUuid,
        isAttending = newAttendance.isAttending
      )
    }
  }
}
