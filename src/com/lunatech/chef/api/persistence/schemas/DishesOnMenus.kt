package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.DishOnMenu
import me.liuwj.ktorm.dsl.QueryRowSet
import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.uuid

object DishesOnMenus : BaseTable<DishOnMenu>("dishes_on_menus") {
    val menuUuid by uuid("menu_uuid")
    val dishUuid by uuid("dish_uuid")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = DishOnMenu(
        menuUuid = row[menuUuid] ?: DEFAULT_UUID,
        dishUuid = row [dishUuid] ?: DEFAULT_UUID
    )
}
