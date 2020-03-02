package com.lunatech.chef.api.persistence.schemas

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid

object Attendances : Table<Attendance>("attendances") {
    val uuid by uuid("uuid").primaryKey().bindTo { it.uuid }
    val scheduleUuuid by uuid("schedule_uuid").references(Schedules) { it.scheduleUuid }
    val userUuid by uuid("user_uuid").references(Users) { it.userUuid }
    val isAttending by boolean("is_attending").bindTo { it.isAttending }
}
