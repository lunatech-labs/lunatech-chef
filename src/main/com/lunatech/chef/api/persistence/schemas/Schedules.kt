package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Schedule
import java.time.LocalDate
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.date
import org.ktorm.schema.uuid

object Schedules : BaseTable<Schedule>("schedules") {
    val uuid = uuid("uuid").primaryKey()
    val menuUuid = uuid("menu_uuid")
    val date = date("date")
    val locationUuid = uuid("location_uuid")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Schedule(
        uuid = row[uuid] ?: DEFAULT_UUID,
        menuUuid = row[menuUuid] ?: DEFAULT_UUID,
        date = row[date] ?: LocalDate.now(),
        locationUuid = row[locationUuid] ?: DEFAULT_UUID,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE
    )
}
