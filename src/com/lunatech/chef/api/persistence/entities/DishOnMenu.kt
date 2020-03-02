package com.lunatech.chef.api.persistence.schemas

import java.util.UUID
import me.liuwj.ktorm.entity.Entity

interface DishOnMenu : Entity<DishOnMenu> {
    val uuid: UUID
    val menuUuid: Menu
    val dishUuid: Dish
    val isDeleted: Boolean
}
