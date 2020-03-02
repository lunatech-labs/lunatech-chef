package com.lunatech.chef.api.persistence

import java.util.UUID

data class DishesOnMenus(
    val uuid: UUID,
    val menuUuid: UUID,
    val dishUuid: UUID,
    val isDeleted: Boolean = false
)
