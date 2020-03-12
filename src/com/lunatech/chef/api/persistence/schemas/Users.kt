package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.User
import me.liuwj.ktorm.dsl.QueryRowSet
import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid
import me.liuwj.ktorm.schema.varchar

object Users : BaseTable<User>("users") {
    val uuid by uuid("uuid").primaryKey()
    val name by varchar("name")
    val emailAddress by varchar("email_address")
    val isAdmin by boolean("is_admin")
    val location by uuid("location")
    val isInactive by boolean("is_inactive")
    val isVegetarian by boolean("is_vegetarian")
    val hasNutsRestriction by boolean("has_nuts_restriction")
    val hasSeafoodRestriction by boolean("has_seafood_restriction")
    val hasPorkRestriction by boolean("has_pork_restriction")
    val hasBeefRestriction by boolean("has_beef_restriction")
    val isGlutenIntolerant by boolean("is_gluten_intolerant")
    val isLactoseIntolerant by boolean("is_lactose_intolerant")
    val otherRestriction by varchar("other_restriction")
    val isDeleted by boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = User(
        uuid = row[uuid] ?: DEFAULT_UUID,
        name = row[name] ?: DEFAULT_STRING,
        emailAddress = row[emailAddress] ?: DEFAULT_STRING,
        isAdmin = row[isAdmin] ?: DEFAULT_FALSE,
        location = row[location] ?: DEFAULT_UUID,
        isInactive = row[isInactive] ?: DEFAULT_FALSE,
        isVegetarian = row[isVegetarian] ?: DEFAULT_FALSE,
        hasNutsRestriction = row[hasNutsRestriction] ?: DEFAULT_FALSE,
        hasSeafoodRestriction = row[hasSeafoodRestriction] ?: DEFAULT_FALSE,
        hasPorkRestriction = row[hasPorkRestriction] ?: DEFAULT_FALSE,
        hasBeefRestriction = row[hasBeefRestriction] ?: DEFAULT_FALSE,
        isGlutenIntolerant = row[isGlutenIntolerant] ?: DEFAULT_FALSE,
        isLactoseIntolerant = row[isLactoseIntolerant] ?: DEFAULT_FALSE,
        otherRestriction = row[otherRestriction] ?: DEFAULT_STRING,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE
    )
}
