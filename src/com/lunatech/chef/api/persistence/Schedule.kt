package com.lunatech.chef.api.persistence

import java.sql.Timestamp
import java.util.UUID

data class Schedule(
    val uuid: UUID,
    val menuUuid: UUID,
    val date: Timestamp,
    val location: UUID,
    val isDeleted: Boolean = false
    )
