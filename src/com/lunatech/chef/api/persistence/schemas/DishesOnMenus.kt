package com.lunatech.chef.api.persistence.schemas

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid

object DishesOnMenus : Table<DishOnMenu>("dishes_on_menus") {
    val uuid by uuid("uuid").primaryKey().bindTo { it.uuid }
    val menuUuid by uuid("menu_uuid").references(Menus) { it.menuUuid }
    val dishUuid by uuid("dish_uuid").references(Dishes) { it.dishUuid }
    val isDeleted by boolean("is_deleted").bindTo { it.isDeleted }
}
