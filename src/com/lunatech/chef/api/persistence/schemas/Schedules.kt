package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Schedule
import me.liuwj.ktorm.dsl.QueryRowSet
import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.date
import me.liuwj.ktorm.schema.uuid
import java.time.LocalDate

object Schedules : BaseTable<Schedule>("schedules") {
    val uuid by uuid("uuid").primaryKey()
    val menuUuid by uuid("menu_uuid")
    val date by date("date")
    val location by uuid("location")
    val isDeleted by boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Schedule(
        uuid = row[uuid] ?: DEFAULT_UUID,
        menuUuid = row[menuUuid] ?: DEFAULT_UUID,
        date = row[date] ?: LocalDate.now(),
        location = row[location] ?: DEFAULT_UUID,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE
    )
}
