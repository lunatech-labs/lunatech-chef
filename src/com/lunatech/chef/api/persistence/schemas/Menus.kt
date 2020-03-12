package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Menu
import me.liuwj.ktorm.dsl.QueryRowSet
import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid
import me.liuwj.ktorm.schema.varchar

object Menus : BaseTable<Menu>("menus") {
    val uuid by uuid("uuid").primaryKey()
    val name by varchar("name")
    val isDeleted by boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Menu(
        uuid = row[uuid] ?: DEFAULT_UUID,
        name = row[name] ?: DEFAULT_STRING,
    isDeleted = row[isDeleted] ?: DEFAULT_FALSE
    )
}
