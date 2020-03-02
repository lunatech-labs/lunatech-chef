package com.lunatech.chef.api.persistence.schemas

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid
import me.liuwj.ktorm.schema.varchar

object Locations : Table<Location>("locations") {
    val uuid by uuid("uuid").primaryKey().bindTo { it.uuid }
    val city by varchar("city").bindTo { it.city }
    val country by varchar("country").bindTo { it.country }
    val isDeleted by boolean("is_deleted").bindTo { it.isDeleted }
}
