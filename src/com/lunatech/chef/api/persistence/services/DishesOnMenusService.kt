package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.DishOnMenu
import com.lunatech.chef.api.persistence.schemas.DishesOnMenus
import com.lunatech.chef.api.routes.UpdatedDishOnMenu
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.dsl.where

class DishesOnMenusService(val database: Database) {
    fun getAll() = database.from(DishesOnMenus).select().where { DishesOnMenus.isDeleted eq false }.map { DishesOnMenus.createEntity(it) }

    fun getByUuid(uuid: UUID): List<DishOnMenu> =
        database.from(DishesOnMenus).select().where { -> DishesOnMenus.uuid eq uuid }.map { DishesOnMenus.createEntity(it) }

    fun insert(dishOnMenu: DishOnMenu): Int =
        database.insert(DishesOnMenus) {
            it.uuid to dishOnMenu.uuid
            it.menuUuid to dishOnMenu.menuUuid
            it.dishUuid to dishOnMenu.dishUuid
            it.isDeleted to dishOnMenu.isDeleted
        }

    fun update(uuid: UUID, dishOnMenu: UpdatedDishOnMenu): Int =
        database.update(DishesOnMenus) {
            it.menuUuid to dishOnMenu.menuUuid
            it.dishUuid to dishOnMenu.dishUuid
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(DishesOnMenus) {
        it.isDeleted to true
        where {
            it.uuid eq uuid
        }
    }
}
