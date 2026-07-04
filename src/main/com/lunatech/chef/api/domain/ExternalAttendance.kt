package com.lunatech.chef.api.domain

import java.time.LocalDate
import java.util.UUID

data class NewExternalAttendance(
    val scheduleUuid: UUID,
)

data class ExternalAttendance(
    val uuid: UUID,
    val scheduleUuid: UUID,
    val attendancesCount: Int = 0,
    val isDeleted: Boolean = false,
) {
    companion object {
        fun fromNewExternalAttendance(newExternalAttendances: NewExternalAttendance): ExternalAttendance =
            ExternalAttendance(
                uuid = UUID.randomUUID(),
                scheduleUuid = newExternalAttendances.scheduleUuid,
            )
    }
}

data class ExternalAttendanceWithInfo(
    val uuid: UUID,
    val scheduleUuid: UUID,
    val menu: MenuWithDishes,
    val date: LocalDate,
    val office: String,
    val attendancesCount: Int,
)
