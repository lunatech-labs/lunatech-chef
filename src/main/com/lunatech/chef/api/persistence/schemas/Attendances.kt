package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Attendance
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.uuid

object Attendances : BaseTable<Attendance>("attendances") {
    val uuid = uuid("uuid").primaryKey()
    val scheduleUuid = uuid("schedule_uuid")
    val userUuid = uuid("user_uuid")
    val isAttending = boolean("is_attending")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Attendance(
        uuid = row[uuid] ?: DEFAULT_UUID,
        scheduleUuid = row[scheduleUuid] ?: DEFAULT_UUID,
        userUuid = row[userUuid] ?: DEFAULT_UUID,
        isAttending = row[isAttending] ?: DEFAULT_FALSE,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE,
    )
}
