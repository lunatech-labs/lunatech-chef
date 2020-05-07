package com.lunatech.chef.api.domain

import java.util.UUID

data class NewUser(
  val name: String,
  val emailAddress: String,
  val isAdmin: Boolean,
  val location: UUID,
  val isVegetarian: Boolean = false,
  val hasNutsRestriction: Boolean = false,
  val hasSeafoodRestriction: Boolean = false,
  val hasPorkRestriction: Boolean = false,
  val hasBeefRestriction: Boolean = false,
  val isGlutenIntolerant: Boolean = false,
  val isLactoseIntolerant: Boolean = false,
  val otherRestriction: String = ""
)

data class User(
  val uuid: UUID,
  val name: String,
  val emailAddress: String,
  val isAdmin: Boolean,
  val location: UUID,
  val isVegetarian: Boolean = false,
  val hasNutsRestriction: Boolean = false,
  val hasSeafoodRestriction: Boolean = false,
  val hasPorkRestriction: Boolean = false,
  val hasBeefRestriction: Boolean = false,
  val isGlutenIntolerant: Boolean = false,
  val isLactoseIntolerant: Boolean = false,
  val otherRestriction: String = "",
  val isInactive: Boolean = false,
  val isDeleted: Boolean = false
) {
  companion object {
    fun fromNewUser(newUser: NewUser): User {
      return User(
        uuid = UUID.randomUUID(),
        name = newUser.name,
        emailAddress = newUser.emailAddress,
        isAdmin = newUser.isAdmin,
        location = newUser.location,
        isVegetarian = newUser.isVegetarian,
        hasNutsRestriction = newUser.hasNutsRestriction,
        hasSeafoodRestriction = newUser.hasSeafoodRestriction,
        hasPorkRestriction = newUser.hasPorkRestriction,
        hasBeefRestriction = newUser.hasBeefRestriction,
        isGlutenIntolerant = newUser.isGlutenIntolerant,
        isLactoseIntolerant = newUser.isLactoseIntolerant,
        otherRestriction = newUser.otherRestriction
      )
    }
  }
}
