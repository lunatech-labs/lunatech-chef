package com.lunatech.chef.api.persistence.schemas

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.jdbcDate
import me.liuwj.ktorm.schema.uuid

object Schedules : Table<Schedule>("schedules") {
    val uuid by uuid("uuid").primaryKey().bindTo { it.uuid }
    val menuUuid by uuid("menu_uuid").references(Menus) { it.menuUuid }
    val date by jdbcDate("date").bindTo { it.date }
    val location by uuid("location").references(Locations) { it.location }
    val isDeleted by boolean("is_deleted").bindTo { it.isDeleted }
}
