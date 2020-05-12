package com.lunatech.chef.api.domain

import java.util.UUID

data class NewMenuWithDishesUuid(
  val name: String,
  val dishesUuid: List<UUID>
)

data class MenuWithDishesUuid(
  val uuid: UUID,
  val name: String,
  val dishesUuid: List<UUID>,
  val isDeleted: Boolean = false
) {
    companion object {
        fun fromNewMenuWithDishesUuid(newMenu: NewMenuWithDishesUuid): MenuWithDishesUuid {
            return MenuWithDishesUuid(
                uuid = UUID.randomUUID(),
                name = newMenu.name,
                dishesUuid = newMenu.dishesUuid
            )
        }
    }
}

data class MenuWithDishes(
  val uuid: UUID,
  val name: String,
  val dishes: List<Dish>
)
