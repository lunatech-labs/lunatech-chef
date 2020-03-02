package com.lunatech.chef.api.domain

import java.util.UUID

data class Menu(
  val uuid: UUID,
  val name: String,
  val isDeleted: Boolean = false
)
