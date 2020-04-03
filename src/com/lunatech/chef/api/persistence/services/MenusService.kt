package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Menu
import com.lunatech.chef.api.persistence.schemas.Menus
import com.lunatech.chef.api.routes.UpdatedMenu
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.dsl.where

class MenusService(val database: Database) {
    fun getAll() = database.from(Menus).select().map { Menus.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Menu> =
        database.from(Menus).select().where { -> Menus.uuid eq uuid }.map { Menus.createEntity(it) }

    fun insert(menu: Menu): Int =
        database.insert(Menus) {
            it.uuid to menu.uuid
            it.name to menu.name
            it.isDeleted to menu.isDeleted
        }

    fun update(uuid: UUID, menu: UpdatedMenu): Int =
        database.update(Menus) {
            it.name to menu.name
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(Menus) {
        it.isDeleted to true
        where {
            it.uuid eq uuid
        }
    }
}
