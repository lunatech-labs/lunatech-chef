package com.lunatech.chef.api.persistence.schemas

import java.util.UUID
import me.liuwj.ktorm.entity.Entity

interface Menu : Entity<Menu> {
    val uuid: UUID
    val name: String
    val isDeleted: Boolean
}
