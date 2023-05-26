package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Dish
import com.lunatech.chef.api.domain.MenuName
import com.lunatech.chef.api.domain.MenuWithDishes
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object MenuNames : BaseTable<MenuName>("menus") {
    val uuid = uuid("uuid").primaryKey()
    val name = varchar("name")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = MenuName(
        uuid = row[uuid] ?: DEFAULT_UUID,
        name = row[name] ?: DEFAULT_STRING,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE,
    )

    fun toMenuWithDishes(menuName: MenuName, dishes: List<Dish>) = MenuWithDishes(
        uuid = menuName.uuid,
        name = menuName.name,
        dishes = dishes,
    )
}
