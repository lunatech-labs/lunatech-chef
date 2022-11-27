package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.DishOnMenu
import com.lunatech.chef.api.domain.MenuWithDishesUuid
import com.lunatech.chef.api.persistence.schemas.DishesOnMenus
import com.lunatech.chef.api.persistence.schemas.MenuNames
import com.lunatech.chef.api.routes.UpdatedMenu
import java.util.UUID
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where

class MenusService(val database: Database) {
    fun getAll(): List<MenuWithDishesUuid> =
        database
            .from(MenuNames)
            .select()
            .where { MenuNames.isDeleted eq false }
            .map { MenuNames.createEntity(it) }
            .map { menuName ->
                val dishes =
                    database
                    .from(DishesOnMenus)
                    .select().where { DishesOnMenus.menuUuid eq menuName.uuid }
                    .map { DishesOnMenus.createEntity(it) }
                    .map { it.dishUuid }

                MenuWithDishesUuid(menuName.uuid, menuName.name, dishes)
            }

    fun getByUuid(uuid: UUID): MenuWithDishesUuid? {
        val menuName =
            database
            .from(MenuNames)
            .select().where { MenuNames.uuid eq uuid }
            .map { MenuNames.createEntity(it) }
            .firstOrNull()

        return menuName?.let {
            val dishes =
                database
                .from(DishesOnMenus)
                .select().where { DishesOnMenus.menuUuid eq it.uuid }
                .map { DishesOnMenus.createEntity(it) }
                .map { it.dishUuid }

            MenuWithDishesUuid(it.uuid, it.name, dishes)
        }
    }

    fun insert(menu: MenuWithDishesUuid): Int {
        // first create a new menu with a name
        database.insert(MenuNames) {
            set(it.uuid, menu.uuid)
            set(it.name, menu.name)
            set(it.isDeleted, menu.isDeleted)
        }

        // second associate the dishes with the new menu
        return menu.dishesUuids.map {
            val dishOnMenu = DishOnMenu(menuUuid = menu.uuid, dishUuid = it)
            database.insert(DishesOnMenus) {
                set(it.menuUuid, dishOnMenu.menuUuid)
                set(it.dishUuid, dishOnMenu.dishUuid)
            }
        }.size
    }

    fun update(uuid: UUID, menu: UpdatedMenu): Int {
        val updatedName = database.update(MenuNames) {
            set(it.name, menu.name)
            where {
                it.uuid eq uuid
            }
        }

        if (updatedName == 1) {
            // the update of dishes id done by removing all current dishes association
            // and then adding new one
            database.delete(DishesOnMenus) { it.menuUuid eq uuid }
            menu.dishesUuids.map { dishUuid ->
                database.insert(DishesOnMenus) {
                    set(it.menuUuid, uuid)
                    set(it.dishUuid, dishUuid)
                }
            }
        }
        return updatedName
    }

    fun delete(uuid: UUID): Int {
        return database.update(MenuNames) {
            set(it.isDeleted, true)
            where {
                it.uuid eq uuid
            }
        }
    }
}
