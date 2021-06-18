package com.lunatech.chef.api.domain

import java.time.LocalDate
import java.util.UUID

data class NewSchedule(
  val menuUuid: UUID,
  val date: LocalDate,
  val locationUuid: UUID
)

data class Schedule(
  val uuid: UUID,
  val menuUuid: UUID,
  val date: LocalDate,
  val locationUuid: UUID,
  val isDeleted: Boolean = false
) {
    companion object {
        fun fromNewSchedule(newSchedule: NewSchedule): Schedule {
            return Schedule(
                uuid = UUID.randomUUID(),
                menuUuid = newSchedule.menuUuid,
                date = newSchedule.date,
                locationUuid = newSchedule.locationUuid
            )
        }
    }
}

data class ScheduleWithMenuInfo(
  val uuid: UUID,
  val menu: MenuWithDishes,
  val date: LocalDate,
  val location: Location
)

data class ScheduleWithAttendanceInfo(
  val uuid: UUID,
  val menuName: String,
  val attendants: List<String>,
  val date: LocalDate,
  val location: Location
)
