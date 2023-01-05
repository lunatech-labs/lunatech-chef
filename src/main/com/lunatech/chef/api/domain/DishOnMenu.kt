package com.lunatech.chef.api.domain

import java.util.UUID

data class DishOnMenu(
  val menuUuid: UUID,
  val dishUuid: UUID
)
