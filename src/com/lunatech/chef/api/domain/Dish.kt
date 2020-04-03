package com.lunatech.chef.api.domain

import java.util.UUID

data class Dish(
  val uuid: UUID,
  val name: String,
  val description: String = "",
  val isVegetarian: Boolean = false,
  val hasNuts: Boolean = false,
  val hasSeafood: Boolean = false,
  val hasPork: Boolean = false,
  val hasBeef: Boolean = false,
  val isGlutenFree: Boolean = false,
  val hasLactose: Boolean = false,
  val isDeleted: Boolean = false
)
