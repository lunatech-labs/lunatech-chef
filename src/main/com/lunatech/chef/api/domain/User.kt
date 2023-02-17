package com.lunatech.chef.api.domain

import java.util.UUID

data class NewUser(
    val name: String,
    val emailAddress: String,
    val locationUuid: UUID?,
    val isVegetarian: Boolean = false,
    val hasHalalRestriction: Boolean = false,
    val hasNutsRestriction: Boolean = false,
    val hasSeafoodRestriction: Boolean = false,
    val hasPorkRestriction: Boolean = false,
    val hasBeefRestriction: Boolean = false,
    val isGlutenIntolerant: Boolean = false,
    val isLactoseIntolerant: Boolean = false,
    val otherRestrictions: String = "",
)

data class User(
    val uuid: UUID,
    val name: String,
    val emailAddress: String,
    val locationUuid: UUID?,
    val isVegetarian: Boolean = false,
    val hasHalalRestriction: Boolean = false,
    val hasNutsRestriction: Boolean = false,
    val hasSeafoodRestriction: Boolean = false,
    val hasPorkRestriction: Boolean = false,
    val hasBeefRestriction: Boolean = false,
    val isGlutenIntolerant: Boolean = false,
    val isLactoseIntolerant: Boolean = false,
    val otherRestrictions: String = "",
    val isInactive: Boolean = false,
    val isDeleted: Boolean = false,
) {
    companion object {
        fun fromNewUser(newUser: NewUser): User {
            return User(
                uuid = UUID.randomUUID(),
                name = newUser.name,
                emailAddress = newUser.emailAddress,
                locationUuid = newUser.locationUuid,
                isVegetarian = newUser.isVegetarian,
                hasHalalRestriction = newUser.hasHalalRestriction,
                hasNutsRestriction = newUser.hasNutsRestriction,
                hasSeafoodRestriction = newUser.hasSeafoodRestriction,
                hasPorkRestriction = newUser.hasPorkRestriction,
                hasBeefRestriction = newUser.hasBeefRestriction,
                isGlutenIntolerant = newUser.isGlutenIntolerant,
                isLactoseIntolerant = newUser.isLactoseIntolerant,
                otherRestrictions = newUser.otherRestrictions,
            )
        }
    }
}
