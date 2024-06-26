package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Schedule
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.date
import org.ktorm.schema.uuid
import java.time.LocalDate

object Schedules : BaseTable<Schedule>("schedules") {
    val uuid = uuid("uuid").primaryKey()
    val menuUuid = uuid("menu_uuid")
    val date = date("date")
    val officeUuid = uuid("office_uuid")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(
        row: QueryRowSet,
        withReferences: Boolean,
    ) = Schedule(
        uuid = row[uuid] ?: DEFAULT_UUID,
        menuUuid = row[menuUuid] ?: DEFAULT_UUID,
        date = row[date] ?: LocalDate.now(),
        officeUuid = row[officeUuid] ?: DEFAULT_UUID,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE,
    )
}
