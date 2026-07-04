package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.ExternalAttendance
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.uuid

object ExternalAttendances : BaseTable<ExternalAttendance>("external_attendances") {
    val uuid = uuid("uuid").primaryKey()
    val scheduleUuid = uuid("schedule_uuid")
    val attendancesCount = int("attendances_count")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(
        row: QueryRowSet,
        withReferences: Boolean,
    ) = ExternalAttendance(
        uuid = row[uuid] ?: DEFAULT_UUID,
        scheduleUuid = row[scheduleUuid] ?: DEFAULT_UUID,
        attendancesCount = row[attendancesCount] ?: DEFAULT_ZERO,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE,
    )
}
