package com.lunatech.chef.api.persistence.schemas

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid
import me.liuwj.ktorm.schema.varchar

object Menus : Table<Menu>("menus") {
    val uuid by uuid("uuid").primaryKey().bindTo { it.uuid }
    val name by varchar("name").bindTo { it.name }
    val isDeleted by boolean("is_deleted").bindTo { it.isDeleted }
}
