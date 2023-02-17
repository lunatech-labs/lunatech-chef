package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Dish
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object Dishes : BaseTable<Dish>("dishes") {
    val uuid = uuid("uuid").primaryKey()
    val name = varchar("name")
    val description = varchar("description")
    val isVegetarian = boolean("is_vegetarian")
    val isHalal = boolean("is_halal")
    val hasNuts = boolean("has_nuts")
    val hasSeafood = boolean("has_seafood")
    val hasPork = boolean("has_pork")
    val hasBeef = boolean("has_beef")
    val isGlutenFree = boolean("is_gluten_free")
    val hasLactose = boolean("has_lactose")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Dish(
        uuid = row[uuid] ?: DEFAULT_UUID,
        name = row[name] ?: DEFAULT_STRING,
        description = row[description] ?: DEFAULT_STRING,
        isVegetarian = row[isVegetarian] ?: DEFAULT_FALSE,
        isHalal = row[isHalal] ?: DEFAULT_FALSE,
        hasNuts = row[hasNuts] ?: DEFAULT_FALSE,
        hasSeafood = row[hasSeafood] ?: DEFAULT_FALSE,
        hasPork = row[hasPork] ?: DEFAULT_FALSE,
        hasBeef = row[hasBeef] ?: DEFAULT_FALSE,
        isGlutenFree = row[isGlutenFree] ?: DEFAULT_FALSE,
        hasLactose = row[hasLactose] ?: DEFAULT_FALSE,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE,
    )
}
