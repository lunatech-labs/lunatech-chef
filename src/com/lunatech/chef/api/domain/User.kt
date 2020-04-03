package com.lunatech.chef.api.domain

import java.util.UUID

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
)
