package com.lunatech.chef.api.domain

import java.time.LocalDate
import java.util.UUID

data class NewRecurrentSchedule(
  val menuUuid: UUID,
  val locationUuid: UUID,
  val repetitionDays: Int,
  val nextDate: LocalDate
)

data class RecurrentSchedule(
  val uuid: UUID,
  val menuUuid: UUID,
  val locationUuid: UUID,
  val repetitionDays: Int,
  val nextDate: LocalDate,
  val isDeleted: Boolean = false
) {
    companion object {
        fun fromNewRecurrentSchedule(newRecurrentSchedule: NewRecurrentSchedule): RecurrentSchedule {
            return RecurrentSchedule(
                uuid = UUID.randomUUID(),
                menuUuid = newRecurrentSchedule.menuUuid,
                locationUuid = newRecurrentSchedule.locationUuid,
                repetitionDays = newRecurrentSchedule.repetitionDays,
                nextDate = newRecurrentSchedule.nextDate
            )
        }
    }
}

data class RecurrentScheduleWithMenuInfo(
  val uuid: UUID,
  val menu: MenuWithDishes,
  val nextDate: LocalDate,
  val location: Location
)
