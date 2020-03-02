package com.lunatech.chef.api.persistence.schemas

import java.sql.Date
import java.util.UUID
import me.liuwj.ktorm.entity.Entity

interface Schedule : Entity<Schedule> {
    val uuid: UUID
    val menuUuid: Menu
    val date: Date
    val location: Location
    val isDeleted: Boolean
}
