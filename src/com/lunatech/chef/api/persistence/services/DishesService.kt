package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Dish
import com.lunatech.chef.api.persistence.schemas.Dishes
import com.lunatech.chef.api.persistence.schemas.DishesOnMenus
import com.lunatech.chef.api.routes.UpdatedDish
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

class DishesService(val database: Database) {
    fun getAll() = database.from(Dishes).select().where { Dishes.isDeleted eq false }.map { Dishes.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Dish> =
        database.from(Dishes).select().where { Dishes.uuid eq uuid }.map { Dishes.createEntity(it) }

    fun insert(dish: Dish): Int =
        database.insert(Dishes) {
            set(it.uuid, dish.uuid)
            set(it.name, dish.name)
            set(it.description, dish.description)
            set(it.isVegetarian, dish.isVegetarian)
            set(it.isHalal, dish.isHalal)
            set(it.hasNuts, dish.hasNuts)
            set(it.hasSeafood, dish.hasSeafood)
            set(it.hasPork, dish.hasPork)
            set(it.hasBeef, dish.hasBeef)
            set(it.isGlutenFree, dish.isGlutenFree)
            set(it.hasLactose, dish.hasLactose)
            set(it.isDeleted, dish.isDeleted)
        }

    fun update(uuid: UUID, dish: UpdatedDish): Int =
        database.update(Dishes) {
            set(it.name, dish.name)
            set(it.description, dish.description)
            set(it.isVegetarian, dish.isVegetarian)
            set(it.isHalal, dish.isHalal)
            set(it.hasNuts, dish.hasNuts)
            set(it.hasSeafood, dish.hasSeafood)
            set(it.hasPork, dish.hasPork)
            set(it.hasBeef, dish.hasBeef)
            set(it.isGlutenFree, dish.isGlutenFree)
            set(it.hasLactose, dish.hasLactose)
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(Dishes) {
        set(it.isDeleted, true)
        where {
            it.uuid eq uuid
        }

        // delete dishes from DishesOnMenus tables
        database.delete(DishesOnMenus) { dish -> dish.dishUuid eq uuid }
    }
}
