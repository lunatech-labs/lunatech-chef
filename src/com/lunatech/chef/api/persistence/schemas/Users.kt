package com.lunatech.chef.api.persistence.schemas

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.uuid
import me.liuwj.ktorm.schema.varchar

object Users : Table<User>("users") {
    val uuid by uuid("uuid").primaryKey().bindTo { it.uuid }
    val name by varchar("name").bindTo { it.name }
    val emailAddress by varchar("email_address").bindTo { it.emailAddress }
    val isAdmin by boolean("is_admin").bindTo { it.isAdmin }
    val location by uuid("location").references(Locations) { it.location }
    val isInactive by boolean("is_inactive").bindTo { it.isInactive }
    val isVegetarian by boolean("is_vegetarian").bindTo { it.isVegetarian }
    val hasNutsRestriction by boolean("has_nuts_restriction").bindTo { it.hasNutsRestriction }
    val hasSeafoodRestriction by boolean("has_seafood_restriction").bindTo { it.hasSeafoodRestriction }
    val hasPorkRestriction by boolean("has_pork_restriction").bindTo { it.hasPorkRestriction }
    val hasBeefRestriction by boolean("has_beef_restriction").bindTo { it.hasBeefRestriction }
    val isGlutenIntolerant by boolean("is_gluten_intolerant").bindTo { it.isGlutenIntolerant }
    val isLactoseIntolerant by boolean("is_lactose_intolerant").bindTo { it.isLactoseIntolerant }
    val otherRestriction by boolean("other_restriction").bindTo { it.otherRestriction }
    val isDeleted by boolean("is_deleted").bindTo { it.isDeleted }
}
