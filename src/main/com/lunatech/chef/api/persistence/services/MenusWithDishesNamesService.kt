package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.MenuWithDishes
import com.lunatech.chef.api.persistence.schemas.Dishes
import com.lunatech.chef.api.persistence.schemas.DishesOnMenus
import com.lunatech.chef.api.persistence.schemas.MenuNames
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import java.util.UUID

class MenusWithDishesNamesService(
    val database: Database,
) {
    fun getAll(): List<MenuWithDishes> =
        database
            .from(MenuNames)
            .select()
            .where { MenuNames.isDeleted eq false }
            .map { MenuNames.createEntity(it) }
            .map { menu ->
                val dishes =
                    database
                        .from(DishesOnMenus)
                        .leftJoin(Dishes, on = DishesOnMenus.dishUuid eq Dishes.uuid)
                        .select()
                        .where { DishesOnMenus.menuUuid eq menu.uuid }

                MenuNames.toMenuWithDishes(menu, dishes.map { Dishes.createEntity(it) })
            }

    fun getByUuid(uuid: UUID): MenuWithDishes? {
        val menuName =
            database
                .from(MenuNames)
                .select()
                .where { MenuNames.uuid eq uuid }
                .map { MenuNames.createEntity(it) }
                .firstOrNull()

        return menuName?.let { it ->
            val dishes =
                database
                    .from(DishesOnMenus)
                    .leftJoin(Dishes, on = DishesOnMenus.dishUuid eq Dishes.uuid)
                    .select()
                    .where { DishesOnMenus.menuUuid eq it.uuid }

            MenuNames.toMenuWithDishes(it, dishes.map { Dishes.createEntity(it) })
        }
    }
}
