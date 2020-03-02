package com.lunatech.chef.api.persistence.schemas

import java.util.UUID
import me.liuwj.ktorm.entity.Entity

interface User : Entity<User> {
    val uuid: UUID
    val name: String
    val emailAddress: String
    val isAdmin: Boolean
    val location: Location
    val isInactive: Boolean
    val isVegetarian: Boolean
    val hasNutsRestriction: Boolean
    val hasSeafoodRestriction: Boolean
    val hasPorkRestriction: Boolean
    val hasBeefRestriction: Boolean
    val isGlutenIntolerant: Boolean
    val isLactoseIntolerant: Boolean
    val otherRestriction: Boolean
    val isDeleted: Boolean
}
