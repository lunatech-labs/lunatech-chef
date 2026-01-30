package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.routes.UpdatedDish
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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

    @Nested
    inner class InsertOperations {
        @Test
        fun `insert returns 1 when dish is successfully created`() {
            val dish = aDish(name = "Pasta Carbonara", description = "Classic Italian pasta", hasPork = true)

            val insertResult = dishesService.insert(dish)

            assertEquals(1, insertResult, "Insert should return 1 for successful creation")
        }

        @Test
        fun `insert persists all dish properties correctly`() {
            val dish = aDish(
                name = "Pasta Carbonara",
                description = "Classic Italian pasta",
                hasPork = true,
            )

            dishesService.insert(dish)
            val retrieved = dishesService.getByUuid(dish.uuid)

            assertEquals(1, retrieved.size)
            assertEquals(dish.name, retrieved[0].name)
            assertEquals(dish.description, retrieved[0].description)
            assertTrue(retrieved[0].hasPork)
        }

        @Test
        fun `insert persists dish with all dietary flags set to true`() {
            val dish = aDish(
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
            )

            dishesService.insert(dish)
            val retrieved = dishesService.getByUuid(dish.uuid)[0]

            assertTrue(retrieved.isVegetarian, "isVegetarian should be true")
            assertTrue(retrieved.isHalal, "isHalal should be true")
            assertTrue(retrieved.hasNuts, "hasNuts should be true")
            assertTrue(retrieved.hasSeafood, "hasSeafood should be true")
            assertTrue(retrieved.hasPork, "hasPork should be true")
            assertTrue(retrieved.hasBeef, "hasBeef should be true")
            assertTrue(retrieved.isGlutenFree, "isGlutenFree should be true")
            assertTrue(retrieved.isLactoseFree, "isLactoseFree should be true")
        }

        @Test
        fun `insert persists dish with all dietary flags set to false by default`() {
            val dish = aDish(name = "Plain Dish")

            dishesService.insert(dish)
            val retrieved = dishesService.getByUuid(dish.uuid)[0]

            assertFalse(retrieved.isVegetarian, "isVegetarian should default to false")
            assertFalse(retrieved.isHalal, "isHalal should default to false")
            assertFalse(retrieved.hasNuts, "hasNuts should default to false")
            assertFalse(retrieved.hasSeafood, "hasSeafood should default to false")
            assertFalse(retrieved.hasPork, "hasPork should default to false")
            assertFalse(retrieved.hasBeef, "hasBeef should default to false")
            assertFalse(retrieved.isGlutenFree, "isGlutenFree should default to false")
            assertFalse(retrieved.isLactoseFree, "isLactoseFree should default to false")
        }
    }

    @Nested
    inner class ReadOperations {
        @Test
        fun `getAll returns only non-deleted dishes`() {
            val dish1 = aDish(name = "Salad", isVegetarian = true)
            val dish2 = aDish(name = "Soup", isVegetarian = true)
            val deletedDish = aDish(name = "Old Dish", isDeleted = true)

            dishesService.insert(dish1)
            dishesService.insert(dish2)
            dishesService.insert(deletedDish)

            val allDishes = dishesService.getAll()

            assertEquals(2, allDishes.size, "Should return only non-deleted dishes")
            assertTrue(allDishes.none { it.uuid == deletedDish.uuid }, "Deleted dish should not be in results")
        }

        @Test
        fun `getAll returns empty list when no dishes exist`() {
            val allDishes = dishesService.getAll()

            assertTrue(allDishes.isEmpty(), "Should return empty list when no dishes exist")
        }

        @Test
        fun `getByUuid returns dish when it exists`() {
            val dish = aDish(name = "Test Dish")
            dishesService.insert(dish)

            val retrieved = dishesService.getByUuid(dish.uuid)

            assertEquals(1, retrieved.size, "Should return exactly one dish")
            assertEquals(dish.uuid, retrieved[0].uuid)
        }

        @Test
        fun `getByUuid returns empty list for non-existent UUID`() {
            val nonExistentUuid = UUID.randomUUID()

            val result = dishesService.getByUuid(nonExistentUuid)

            assertTrue(result.isEmpty(), "Should return empty list for non-existent UUID")
        }

        @Test
        fun `getByUuid returns deleted dish without filtering`() {
            val deletedDish = aDish(name = "Deleted Dish", isDeleted = true)
            dishesService.insert(deletedDish)

            val retrieved = dishesService.getByUuid(deletedDish.uuid)

            assertEquals(1, retrieved.size, "getByUuid should return deleted dishes")
            assertTrue(retrieved[0].isDeleted)
        }
    }

    @Nested
    inner class UpdateOperations {
        @Test
        fun `update returns 1 when dish is successfully updated`() {
            val dish = aDish(name = "Salad", isVegetarian = true)
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
                isLactoseFree = false,
            )

            val updateResult = dishesService.update(dish.uuid, updatedDish)

            assertEquals(1, updateResult, "Update should return 1 for successful update")
        }

        @Test
        fun `update modifies all dish properties`() {
            val dish = aDish(name = "Salad", description = "Original description", isVegetarian = true)
            dishesService.insert(dish)

            val updatedDish = UpdatedDish(
                name = "Caesar Salad",
                description = "With croutons",
                isVegetarian = false,
                isHalal = true,
                hasNuts = true,
                hasSeafood = false,
                hasPork = false,
                hasBeef = false,
                isGlutenFree = false,
                isLactoseFree = false,
            )
            dishesService.update(dish.uuid, updatedDish)

            val retrieved = dishesService.getByUuid(dish.uuid)[0]

            assertEquals("Caesar Salad", retrieved.name)
            assertEquals("With croutons", retrieved.description)
            assertFalse(retrieved.isVegetarian)
            assertTrue(retrieved.isHalal)
            assertTrue(retrieved.hasNuts)
        }

        @Test
        fun `update returns 0 for non-existent dish`() {
            val nonExistentUuid = UUID.randomUUID()
            val updatedDish = UpdatedDish(
                name = "Updated Name",
                description = "Updated description",
                isVegetarian = false,
                isHalal = false,
                hasNuts = false,
                hasSeafood = false,
                hasPork = false,
                hasBeef = false,
                isGlutenFree = false,
                isLactoseFree = false,
            )

            val updateResult = dishesService.update(nonExistentUuid, updatedDish)

            assertEquals(0, updateResult, "Update should return 0 for non-existent dish")
        }
    }

    @Nested
    inner class DeleteOperations {
        @Test
        fun `delete performs soft delete by setting isDeleted flag`() {
            val dish = aDish(name = "Dish to delete")
            dishesService.insert(dish)

            dishesService.delete(dish.uuid)

            val retrieved = dishesService.getByUuid(dish.uuid)
            assertEquals(1, retrieved.size, "Dish should still exist in database")
            assertTrue(retrieved[0].isDeleted, "isDeleted flag should be set to true")
        }

        @Test
        fun `delete removes dish from getAll results`() {
            val dish = aDish(name = "Dish to delete")
            dishesService.insert(dish)

            dishesService.delete(dish.uuid)

            val allDishes = dishesService.getAll()
            assertTrue(allDishes.isEmpty(), "Deleted dish should not appear in getAll")
        }

        @Test
        fun `delete returns 0 for non-existent dish`() {
            val nonExistentUuid = UUID.randomUUID()

            val deleteResult = dishesService.delete(nonExistentUuid)

            assertEquals(0, deleteResult, "Delete should return 0 for non-existent dish")
        }
    }
}
