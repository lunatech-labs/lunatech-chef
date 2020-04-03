package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Dish
import me.liuwj.ktorm.dsl.QueryRowSet
import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid
import me.liuwj.ktorm.schema.varchar

object Dishes : BaseTable<Dish>("dishes") {
    val uuid by uuid("uuid").primaryKey()
    val name by varchar("name")
    val description by varchar("description")
    val isVegetarian by boolean("is_vegetarian")
    val hasSeafood by boolean("has_seafood")
    val hasPork by boolean("has_pork")
    val hasBeef by boolean("has_beef")
    val isGlutenFree by boolean("is_gluten_free")
    val hasLactose by boolean("has_lactose")
    val isDeleted by boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Dish(
        uuid = row[uuid] ?: DEFAULT_UUID,
        name = row[name] ?: DEFAULT_STRING,
        description = row[description] ?: DEFAULT_STRING,
        isVegetarian = row[isVegetarian] ?: DEFAULT_FALSE,
        hasSeafood = row[hasSeafood] ?: DEFAULT_FALSE,
        hasPork = row[hasPork] ?: DEFAULT_FALSE,
        hasBeef = row[hasBeef] ?: DEFAULT_FALSE,
        isGlutenFree = row[isGlutenFree] ?: DEFAULT_FALSE,
        hasLactose = row[hasLactose] ?: DEFAULT_FALSE,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE
    )
}
