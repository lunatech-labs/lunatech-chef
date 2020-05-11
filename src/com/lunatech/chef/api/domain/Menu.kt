package com.lunatech.chef.api.domain

import java.util.UUID

data class NewMenu(
  val name: String,
  val dishesUuid: List<UUID>
)

data class Menu(
  val uuid: UUID,
  val name: String,
  val dishesUuid: List<UUID>,
  val isDeleted: Boolean = false
) {
    companion object {
        fun fromNewMenu(newMenu: NewMenu): Menu {
            return Menu(
                uuid = UUID.randomUUID(),
                name = newMenu.name,
                dishesUuid = newMenu.dishesUuid
            )
        }
    }
}
