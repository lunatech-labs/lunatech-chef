package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.ExternalAttendance
import com.lunatech.chef.api.persistence.schemas.ExternalAttendances
import com.lunatech.chef.api.routes.UpdatedExternalAttendance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import java.util.UUID

class ExternalAttendancesService(
    private val database: Database,
) {
    fun insert(externalAttendance: ExternalAttendance): Int =
        database.insert(ExternalAttendances) {
            set(it.uuid, externalAttendance.uuid)
            set(it.scheduleUuid, externalAttendance.scheduleUuid)
            set(it.attendancesCount, externalAttendance.attendancesCount)
            set(it.isDeleted, externalAttendance.isDeleted)
        }

    fun update(
        uuid: UUID,
        externalAttendance: UpdatedExternalAttendance,
    ): Int =
        database.update(ExternalAttendances) {
            set(it.attendancesCount, externalAttendance.attendancesCount)
            where {
                it.uuid eq uuid
            }
        }
}
