package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Attendance
import me.liuwj.ktorm.dsl.QueryRowSet
import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid

object Attendances : BaseTable<Attendance>("attendances") {
    val uuid by uuid("uuid").primaryKey()
    val scheduleUuuid by uuid("schedule_uuid")
    val userUuid by uuid("user_uuid")
    val isAttending by boolean("is_attending")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Attendance(
        uuid = row[uuid] ?: DEFAULT_UUID,
        scheduleUuid = row[scheduleUuuid] ?: DEFAULT_UUID,
        userUuid = row[userUuid] ?: DEFAULT_UUID,
        isAttending = row[isAttending] ?: DEFAULT_FALSE
    )
}
