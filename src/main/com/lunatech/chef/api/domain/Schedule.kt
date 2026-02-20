package com.lunatech.chef.api.domain

import java.time.LocalDate
import java.util.UUID

data class NewSchedule(
    val menuUuid: UUID,
    val date: LocalDate,
    val officeUuid: UUID,
)

data class Schedule(
    val uuid: UUID,
    val menuUuid: UUID,
    val date: LocalDate,
    val officeUuid: UUID,
    val isDeleted: Boolean = false,
) {
    companion object {
        fun fromNewSchedule(newSchedule: NewSchedule): Schedule =
            Schedule(
                uuid = UUID.randomUUID(),
                menuUuid = newSchedule.menuUuid,
                date = newSchedule.date,
                officeUuid = newSchedule.officeUuid,
            )
    }
}

data class ScheduleWithMenuInfo(
    val uuid: UUID,
    val menu: MenuWithDishes,
    val date: LocalDate,
    val office: Office,
)

data class ScheduleWithAttendanceInfo(
    val uuid: UUID,
    val menuName: String,
    val attendants: List<User>,
    val date: LocalDate,
    val office: String,
)
