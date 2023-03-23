package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Attendance
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.uuid
import org.ktorm.schema.datetime
import org.ktorm.schema.date

object Attendances : BaseTable<Attendance>("attendances") {
    val uuid = uuid("uuid").primaryKey()
    val scheduleUuid = uuid("schedule_uuid")
    val userUuid = uuid("user_uuid")
    val isAttending = boolean("is_attending")
    val isDeleted = boolean("is_deleted")
    val createdAt = date("created_at")
    val updatedAt = date("updated_at")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Attendance(
        uuid = row[uuid] ?: DEFAULT_UUID,
        scheduleUuid = row[scheduleUuid] ?: DEFAULT_UUID,
        userUuid = row[userUuid] ?: DEFAULT_UUID,
        isAttending = row[isAttending] ?: DEFAULT_FALSE,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE,
        createdAt = row[createdAt] ?: DEFAULT_DATE,
        updatedAt = row[updatedAt] ?: DEFAULT_DATE,
    )
}
