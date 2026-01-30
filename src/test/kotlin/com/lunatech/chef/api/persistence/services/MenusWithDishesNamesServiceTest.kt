package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class MenusWithDishesNamesServiceTest {
    private lateinit var menusWithDishesNamesService: MenusWithDishesNamesService
    private lateinit var menusService: MenusService
    private lateinit var dishesService: DishesService

    private lateinit var testDish1Uuid: UUID
    private lateinit var testDish2Uuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        menusWithDishesNamesService = MenusWithDishesNamesService(database)
        menusService = MenusService(database)
        dishesService = DishesService(database)

        // Create test dishes
        val testDish1 = aDish(name = "Pasta Carbonara", description = "Classic Italian pasta", hasPork = true)
        val testDish2 = aDish(name = "Caesar Salad", description = "Fresh salad with croutons", isVegetarian = true)
        dishesService.insert(testDish1)
        dishesService.insert(testDish2)
        testDish1Uuid = testDish1.uuid
        testDish2Uuid = testDish2.uuid
    }

    @Nested
    inner class GetAllOperations {
        @Test
        fun `getAll returns menus with full dish details`() {
            val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish1Uuid, testDish2Uuid))
            menusService.insert(menu)

            val allMenus = menusWithDishesNamesService.getAll()

            assertEquals(1, allMenus.size)
            val retrievedMenu = allMenus[0]
            assertEquals(menu.name, retrievedMenu.name)
            assertEquals(2, retrievedMenu.dishes.size)

            val dishNames = retrievedMenu.dishes.map { it.name }
            assertTrue(dishNames.contains("Pasta Carbonara"))
            assertTrue(dishNames.contains("Caesar Salad"))
        }

        @Test
        fun `getAll returns only non-deleted menus`() {
            val menu1 = aMenu(name = "Menu 1", dishesUuids = listOf(testDish1Uuid))
            val menu2 = aMenu(name = "Menu 2", dishesUuids = listOf(testDish2Uuid))
            val deletedMenu = aMenu(name = "Deleted Menu", dishesUuids = emptyList(), isDeleted = true)

            menusService.insert(menu1)
            menusService.insert(menu2)
            menusService.insert(deletedMenu)

            val allMenus = menusWithDishesNamesService.getAll()

            assertEquals(2, allMenus.size, "Should return only non-deleted menus")
            assertTrue(allMenus.none { it.uuid == deletedMenu.uuid })
        }

        @Test
        fun `getAll returns empty list when no menus exist`() {
            val allMenus = menusWithDishesNamesService.getAll()

            assertTrue(allMenus.isEmpty(), "Should return empty list when no menus exist")
        }

        @Test
        fun `getAll returns menus with dishes preserving all dish properties`() {
            val menu = aMenu(name = "Full Menu", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val allMenus = menusWithDishesNamesService.getAll()
            val retrievedDish = allMenus[0].dishes[0]

            assertEquals("Pasta Carbonara", retrievedDish.name)
            assertEquals("Classic Italian pasta", retrievedDish.description)
            assertTrue(retrievedDish.hasPork)
        }
    }

    @Nested
    inner class GetByUuidOperations {
        @Test
        fun `getByUuid returns menu with full dish details`() {
            val menu = aMenu(name = "Special Menu", dishesUuids = listOf(testDish1Uuid, testDish2Uuid))
            menusService.insert(menu)

            val retrieved = menusWithDishesNamesService.getByUuid(menu.uuid)

            assertEquals(menu.name, retrieved?.name)
            assertEquals(2, retrieved?.dishes?.size)

            // Check that dish details are fully loaded
            val pasta = retrieved?.dishes?.find { it.name == "Pasta Carbonara" }
            assertEquals("Classic Italian pasta", pasta?.description)
            assertTrue(pasta?.hasPork == true)

            val salad = retrieved?.dishes?.find { it.name == "Caesar Salad" }
            assertEquals("Fresh salad with croutons", salad?.description)
            assertTrue(salad?.isVegetarian == true)
        }

        @Test
        fun `getByUuid returns null for non-existent menu`() {
            val result = menusWithDishesNamesService.getByUuid(UUID.randomUUID())

            assertNull(result, "Should return null for non-existent menu")
        }

        @Test
        fun `getByUuid returns menu with empty dish list when menu has no dishes`() {
            val menu = aMenu(name = "Empty Menu", dishesUuids = emptyList())
            menusService.insert(menu)

            val retrieved = menusWithDishesNamesService.getByUuid(menu.uuid)

            assertEquals(menu.name, retrieved?.name)
            assertTrue(retrieved?.dishes?.isEmpty() == true)
        }
    }
}
