package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.User
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object Users : BaseTable<User>("users") {
    val uuid = uuid("uuid").primaryKey()
    val name = varchar("name")
    val emailAddress = varchar("email_address")
    val locationUuid = uuid("location_uuid")
    val isVegetarian = boolean("is_vegetarian")
    val hasHalalRestriction = boolean("has_halal_restriction")
    val hasNutsRestriction = boolean("has_nuts_restriction")
    val hasSeafoodRestriction = boolean("has_seafood_restriction")
    val hasPorkRestriction = boolean("has_pork_restriction")
    val hasBeefRestriction = boolean("has_beef_restriction")
    val isGlutenIntolerant = boolean("is_gluten_intolerant")
    val isLactoseIntolerant = boolean("is_lactose_intolerant")
    val otherRestrictions = varchar("other_restrictions")
    val isInactive = boolean("is_inactive")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = User(
        uuid = row[uuid] ?: DEFAULT_UUID,
        name = row[name] ?: DEFAULT_STRING,
        emailAddress = row[emailAddress] ?: DEFAULT_STRING,
        locationUuid = row[locationUuid] ?: DEFAULT_UUID,
        isVegetarian = row[isVegetarian] ?: DEFAULT_FALSE,
        hasHalalRestriction = row[hasHalalRestriction] ?: DEFAULT_FALSE,
        hasNutsRestriction = row[hasNutsRestriction] ?: DEFAULT_FALSE,
        hasSeafoodRestriction = row[hasSeafoodRestriction] ?: DEFAULT_FALSE,
        hasPorkRestriction = row[hasPorkRestriction] ?: DEFAULT_FALSE,
        hasBeefRestriction = row[hasBeefRestriction] ?: DEFAULT_FALSE,
        isGlutenIntolerant = row[isGlutenIntolerant] ?: DEFAULT_FALSE,
        isLactoseIntolerant = row[isLactoseIntolerant] ?: DEFAULT_FALSE,
        otherRestrictions = row[otherRestrictions] ?: DEFAULT_STRING,
        isInactive = row[isInactive] ?: DEFAULT_FALSE,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE,
    )
}
