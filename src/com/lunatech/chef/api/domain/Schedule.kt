package com.lunatech.chef.api.domain

import java.time.LocalDate
import java.util.UUID

data class NewSchedule(
  val menuUuid: UUID,
  val date: LocalDate,
  val location: UUID
)

data class Schedule(
  val uuid: UUID,
  val menuUuid: UUID,
  val date: LocalDate,
  val location: UUID,
  val isDeleted: Boolean = false
) {
    companion object {
        fun fromNewSchedule(newSchedule: NewSchedule): Schedule {
            return Schedule(
                uuid = UUID.randomUUID(),
                menuUuid = newSchedule.menuUuid,
                date = newSchedule.date,
                location = newSchedule.location
            )
        }
    }
}
