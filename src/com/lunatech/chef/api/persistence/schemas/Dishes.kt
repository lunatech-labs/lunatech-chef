package com.lunatech.chef.api.persistence.schemas

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid
import me.liuwj.ktorm.schema.varchar

object Dishes : Table<Dish>("dish") {
    val uuid by uuid("uuid").primaryKey().bindTo { it.uuid }
    val name by varchar("name").bindTo { it.name }
    val description by varchar("description").bindTo { it.description }
    val isVegetarian by boolean("is_vegetarian").bindTo { it.isVegetarian }
    val hasSeafood by boolean("has_seafood").bindTo { it.hasSeafood }
    val hasPork by boolean("has_pork").bindTo { it.hasPork }
    val hasBeef by boolean("has_beef").bindTo { it.hasBeef }
    val isGlutenFree by boolean("is_gluten_free").bindTo { it.isGlutenFree }
    val hasLactose by boolean("has_lactose").bindTo { it.hasLactose }
    val isDeleted by boolean("is_deleted").bindTo { it.isDeleted }
}
