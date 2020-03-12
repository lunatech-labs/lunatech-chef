package com.lunatech.chef.api.domain

import java.time.LocalDate
import java.util.UUID

data class Schedule(
  val uuid: UUID,
  val menuUuid: UUID,
  val date: LocalDate,
  val location: UUID,
  val isDeleted: Boolean = false
)
