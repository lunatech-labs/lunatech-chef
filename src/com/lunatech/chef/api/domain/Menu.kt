package com.lunatech.chef.api.domain

import java.util.UUID

data class NewMenu(
  val name: String
)

data class Menu(
  val uuid: UUID,
  val name: String,
  val isDeleted: Boolean = false
) {
  companion object {
    fun fromNewMenu(newMenu: NewMenu): Menu {
      return Menu(
        uuid = UUID.randomUUID(),
        name = newMenu.name
      )
    }
  }
}

