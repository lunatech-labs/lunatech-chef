package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.DishOnMenu
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.uuid

object DishesOnMenus : BaseTable<DishOnMenu>("dishes_on_menus") {
    val menuUuid = uuid("menu_uuid")
    val dishUuid = uuid("dish_uuid")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = DishOnMenu(
        menuUuid = row[menuUuid] ?: DEFAULT_UUID,
        dishUuid = row[dishUuid] ?: DEFAULT_UUID,
    )
}
