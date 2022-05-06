package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.RecurrentSchedule
import java.time.LocalDate
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.uuid

object RecurrentSchedules : BaseTable<RecurrentSchedule>("recurrent_schedules") {
    val uuid = uuid("uuid").primaryKey()
    val menuUuid = uuid("menu_uuid")
    val locationUuid = uuid("location_uuid")
    val repetitionDays = int("repetition_days")
    val nextDate = date("next_date")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = RecurrentSchedule(
        uuid = row[uuid] ?: DEFAULT_UUID,
        menuUuid = row[menuUuid] ?: DEFAULT_UUID,
        locationUuid = row[Schedules.locationUuid] ?: DEFAULT_UUID,
        repetitionDays = row[repetitionDays] ?: DEFAULT_RECURRENCY,
        nextDate = row[nextDate] ?: LocalDate.now(),
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE
    )
}
