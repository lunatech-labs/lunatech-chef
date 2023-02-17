package com.lunatech.chef.api.domain

import java.util.UUID

data class NewDish(
    val name: String,
    val description: String = "",
    val isVegetarian: Boolean = false,
    val isHalal: Boolean = false,
    val hasNuts: Boolean = false,
    val hasSeafood: Boolean = false,
    val hasPork: Boolean = false,
    val hasBeef: Boolean = false,
    val isGlutenFree: Boolean = false,
    val hasLactose: Boolean = false,
)

data class Dish(
    val uuid: UUID,
    val name: String,
    val description: String = "",
    val isVegetarian: Boolean = false,
    val isHalal: Boolean = false,
    val hasNuts: Boolean = false,
    val hasSeafood: Boolean = false,
    val hasPork: Boolean = false,
    val hasBeef: Boolean = false,
    val isGlutenFree: Boolean = false,
    val hasLactose: Boolean = false,
    val isDeleted: Boolean = false,
) {
    companion object {
        fun fromNewDish(newDish: NewDish): Dish {
            return Dish(
                uuid = UUID.randomUUID(),
                name = newDish.name,
                description = newDish.description,
                isVegetarian = newDish.isVegetarian,
                isHalal = newDish.isHalal,
                hasNuts = newDish.hasNuts,
                hasSeafood = newDish.hasSeafood,
                hasPork = newDish.hasPork,
                hasBeef = newDish.hasBeef,
                isGlutenFree = newDish.isGlutenFree,
                hasLactose = newDish.hasLactose,
            )
        }
    }
}
