package com.lunatech.chef.api.domain

import java.util.UUID

data class Attendance(
  val uuid: UUID,
  val scheduleUuid: UUID,
  val userUuid: UUID,
  val isAttending: Boolean
)
