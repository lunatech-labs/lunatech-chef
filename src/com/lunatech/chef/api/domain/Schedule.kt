package com.lunatech.chef.api.domain

import java.sql.Date
import java.util.UUID

data class Schedule(
  val uuid: UUID,
  val menuUuid: UUID,
  val date: Date,
  val location: UUID,
  val isDeleted: Boolean = false
)
