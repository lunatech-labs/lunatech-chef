package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Dish
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.routes.UpdatedDish
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class DishesServiceTest {
    private lateinit var dishesService: DishesService

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        dishesService = DishesService(database)
    }

    @Test
    fun `insert and retrieve dish`() {
        val dish = Dish(
            uuid = UUID.randomUUID(),
            name = "Pasta Carbonara",
            description = "Classic Italian pasta",
            isVegetarian = false,
            isHalal = false,
            hasNuts = false,
            hasSeafood = false,
            hasPork = true,
            hasBeef = false,
            isGlutenFree = false,
            isLactoseFree = false,
            isDeleted = false
        )

        val insertResult = dishesService.insert(dish)
        assertEquals(1, insertResult)

        val retrieved = dishesService.getByUuid(dish.uuid)
        assertEquals(1, retrieved.size)
        assertEquals(dish.name, retrieved[0].name)
        assertEquals(dish.description, retrieved[0].description)
        assertTrue(retrieved[0].hasPork)
    }

    @Test
    fun `getAll returns only non-deleted dishes`() {
        val dish1 = Dish(UUID.randomUUID(), "Salad", "Fresh salad", isVegetarian = true)
        val dish2 = Dish(UUID.randomUUID(), "Soup", "Hot soup", isVegetarian = true)
        val deletedDish = Dish(UUID.randomUUID(), "Old Dish", "Deleted", isDeleted = true)

        dishesService.insert(dish1)
        dishesService.insert(dish2)
        dishesService.insert(deletedDish)

        val allDishes = dishesService.getAll()
        assertEquals(2, allDishes.size)
        assertTrue(allDishes.none { it.uuid == deletedDish.uuid })
    }

    @Test
    fun `update dish`() {
        val dish = Dish(UUID.randomUUID(), "Salad", "Fresh salad", isVegetarian = true)
        dishesService.insert(dish)

        val updatedDish = UpdatedDish(
            name = "Caesar Salad",
            description = "With croutons",
            isVegetarian = false,
            isHalal = false,
            hasNuts = false,
            hasSeafood = false,
            hasPork = false,
            hasBeef = false,
            isGlutenFree = false,
            isLactoseFree = false
        )
        val updateResult = dishesService.update(dish.uuid, updatedDish)
        assertEquals(1, updateResult)

        val retrieved = dishesService.getByUuid(dish.uuid)
        assertEquals("Caesar Salad", retrieved[0].name)
        assertEquals("With croutons", retrieved[0].description)
    }

    @Test
    fun `delete dish soft deletes`() {
        val dish = Dish(UUID.randomUUID(), "Salad", "Fresh salad", isVegetarian = true)
        dishesService.insert(dish)

        dishesService.delete(dish.uuid)

        val allDishes = dishesService.getAll()
        assertTrue(allDishes.isEmpty())

        val retrieved = dishesService.getByUuid(dish.uuid)
        assertEquals(1, retrieved.size)
        assertTrue(retrieved[0].isDeleted)
    }

    @Test
    fun `insert dish with all dietary flags`() {
        val dish = Dish(
            uuid = UUID.randomUUID(),
            name = "Special Dish",
            description = "All flags set",
            isVegetarian = true,
            isHalal = true,
            hasNuts = true,
            hasSeafood = true,
            hasPork = true,
            hasBeef = true,
            isGlutenFree = true,
            isLactoseFree = true,
            isDeleted = false
        )

        dishesService.insert(dish)

        val retrieved = dishesService.getByUuid(dish.uuid)[0]
        assertTrue(retrieved.isVegetarian)
        assertTrue(retrieved.isHalal)
        assertTrue(retrieved.hasNuts)
        assertTrue(retrieved.hasSeafood)
        assertTrue(retrieved.hasPork)
        assertTrue(retrieved.hasBeef)
        assertTrue(retrieved.isGlutenFree)
        assertTrue(retrieved.isLactoseFree)
    }
}
