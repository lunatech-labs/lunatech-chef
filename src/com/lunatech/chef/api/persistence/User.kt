package com.lunatech.chef.api.persistence

import java.util.UUID

data class User(
    val uuid: UUID,
    val name: String,
    val emailAddress: String,
    val isAdmin: Boolean,
    val location: UUID,
    val isInactive: Boolean = false,
    val isVegetarian: Boolean = false,
    val hasNutsRestriction: Boolean = false,
    val hasSeafoodRestriction: Boolean = false,
    val hasPorkRestriction: Boolean = false,
    val hasBeefRestriction: Boolean = false,
    val isGlutenIntolerant: Boolean = false,
    val isLactoseIntolerant: Boolean = false,
    val otherRestriction: Boolean = false,
    val isDeleted: Boolean = false
    )
