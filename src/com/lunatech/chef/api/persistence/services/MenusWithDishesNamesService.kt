package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.MenuWithDishes
import com.lunatech.chef.api.persistence.schemas.Dishes
import com.lunatech.chef.api.persistence.schemas.DishesOnMenus
import com.lunatech.chef.api.persistence.schemas.MenuNames
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.leftJoin
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.where

class MenusWithDishesNamesService(val database: Database) {
    fun getAll(): List<MenuWithDishes> =
        database
            .from(MenuNames)
            .select()
            .where { MenuNames.isDeleted eq false }
            .map { MenuNames.createEntity(it) }
            .map { menu ->
                val dishes = database
                    .from(DishesOnMenus)
                    .leftJoin(Dishes, on = DishesOnMenus.dishUuid eq Dishes.uuid)
                    .select()
                    .where { DishesOnMenus.menuUuid eq menu.uuid }

                MenuNames.toMenuWithDishes(menu, dishes)
            }

    fun getByUuid(uuid: UUID): MenuWithDishes? {
        val menuName =
            database
                .from(MenuNames)
                .select().where { -> (MenuNames.uuid eq uuid) and (MenuNames.isDeleted eq false) }
                .map { MenuNames.createEntity(it) }
                .firstOrNull()

        return menuName?.let {
            val dishes =
                database
                    .from(DishesOnMenus)
                    .leftJoin(Dishes, on = DishesOnMenus.dishUuid eq Dishes.uuid)
                    .select().where { DishesOnMenus.menuUuid eq it.uuid }

            MenuNames.toMenuWithDishes(it, dishes)
        }
    }
}
