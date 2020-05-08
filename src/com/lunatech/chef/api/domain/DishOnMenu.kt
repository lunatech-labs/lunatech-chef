package com.lunatech.chef.api.domain

import java.util.UUID

data class NewDishOnMenu(
  val menuUuid: UUID,
  val dishUuid: UUID
)

data class DishOnMenu(
  val uuid: UUID,
  val menuUuid: UUID,
  val dishUuid: UUID,
  val isDeleted: Boolean = false
) {
    companion object {
        fun fromNewDishOnMenu(newDishOnMenu: NewDishOnMenu): DishOnMenu {
            return DishOnMenu(
                uuid = UUID.randomUUID(),
                menuUuid = newDishOnMenu.menuUuid,
                dishUuid = newDishOnMenu.dishUuid
            )
        }
    }
}
