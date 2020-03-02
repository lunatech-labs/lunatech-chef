package com.lunatech.chef.api.persistence.schemas

import java.util.UUID
import me.liuwj.ktorm.entity.Entity

interface Location : Entity<Location> {
    val uuid: UUID
    val city: String
    val country: String
    val isDeleted: Boolean
}
