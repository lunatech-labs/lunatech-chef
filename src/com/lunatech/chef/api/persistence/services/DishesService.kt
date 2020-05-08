package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Dish
import com.lunatech.chef.api.persistence.schemas.Dishes
import com.lunatech.chef.api.routes.UpdatedDish
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.dsl.where

class DishesService(val database: Database) {
    fun getAll() = database.from(Dishes).select().where { Dishes.isDeleted eq false }.map { Dishes.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Dish> =
        database.from(Dishes).select().where { -> Dishes.uuid eq uuid }.map { Dishes.createEntity(it) }

    fun insert(dish: Dish): Int =
        database.insert(Dishes) {
            it.uuid to dish.uuid
            it.name to dish.name
            it.description to dish.description
            it.isVegetarian to dish.isVegetarian
            it.hasNuts to dish.hasNuts
            it.hasSeafood to dish.hasSeafood
            it.hasPork to dish.hasPork
            it.hasBeef to dish.hasBeef
            it.isGlutenFree to dish.isGlutenFree
            it.hasLactose to dish.hasLactose
            it.isDeleted to dish.isDeleted
        }

    fun update(uuid: UUID, dish: UpdatedDish): Int =
        database.update(Dishes) {
            it.name to dish.name
            it.description to dish.description
            it.isVegetarian to dish.isVegetarian
            it.hasNuts to dish.hasNuts
            it.hasSeafood to dish.hasSeafood
            it.hasPork to dish.hasPork
            it.hasBeef to dish.hasBeef
            it.isGlutenFree to dish.isGlutenFree
            it.hasLactose to dish.hasLactose
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(Dishes) {
        it.isDeleted to true
        where {
            it.uuid eq uuid
        }
    }
}
