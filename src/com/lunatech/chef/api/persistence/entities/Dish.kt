package com.lunatech.chef.api.persistence.schemas

import java.util.UUID
import me.liuwj.ktorm.entity.Entity

interface Dish : Entity<Dish> {
    val uuid: UUID
    val name: String
    val description: String
    val isVegetarian: Boolean
    val hasSeafood: Boolean
    val hasPork: Boolean
    val hasBeef: Boolean
    val isGlutenFree: Boolean
    val hasLactose: Boolean
    val isDeleted: Boolean
}
