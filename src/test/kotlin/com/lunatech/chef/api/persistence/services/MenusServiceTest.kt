package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.routes.UpdatedMenu
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class MenusServiceTest {
    private lateinit var menusService: MenusService
    private lateinit var dishesService: DishesService
    private lateinit var testDish1Uuid: UUID
    private lateinit var testDish2Uuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        menusService = MenusService(database)
        dishesService = DishesService(database)

        // Create test dishes for menus
        val testDish1 = aDish(name = "Pasta", description = "Italian pasta", isVegetarian = true)
        val testDish2 = aDish(name = "Salad", description = "Fresh salad", isVegetarian = true)
        dishesService.insert(testDish1)
        dishesService.insert(testDish2)
        testDish1Uuid = testDish1.uuid
        testDish2Uuid = testDish2.uuid
    }

    @Nested
    inner class InsertOperations {
        @Test
        fun `insert returns number of dishes associated when menu is created`() {
            val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish1Uuid, testDish2Uuid))

            val insertResult = menusService.insert(menu)

            assertEquals(2, insertResult, "Insert should return the number of dishes associated")
        }

        @Test
        fun `insert persists menu with all dish associations`() {
            val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish1Uuid, testDish2Uuid))

            menusService.insert(menu)
            val retrieved = menusService.getByUuid(menu.uuid)

            assertEquals(menu.name, retrieved?.name)
            assertEquals(2, retrieved?.dishesUuids?.size)
            assertTrue(retrieved?.dishesUuids?.contains(testDish1Uuid) == true)
            assertTrue(retrieved?.dishesUuids?.contains(testDish2Uuid) == true)
        }

        @Test
        fun `insert returns 0 when menu has no dishes`() {
            val menu = aMenu(name = "Empty Menu", dishesUuids = emptyList())

            val insertResult = menusService.insert(menu)

            assertEquals(0, insertResult, "Insert should return 0 for menu with no dishes")
        }

        @Test
        fun `insert persists menu with no dishes correctly`() {
            val menu = aMenu(name = "Empty Menu", dishesUuids = emptyList())

            menusService.insert(menu)
            val retrieved = menusService.getByUuid(menu.uuid)

            assertEquals(menu.name, retrieved?.name)
            assertTrue(retrieved?.dishesUuids?.isEmpty() == true, "Dishes list should be empty")
        }
    }

    @Nested
    inner class ReadOperations {
        @Test
        fun `getAll returns only non-deleted menus`() {
            val menu1 = aMenu(name = "Menu 1", dishesUuids = listOf(testDish1Uuid))
            val menu2 = aMenu(name = "Menu 2", dishesUuids = listOf(testDish2Uuid))
            val deletedMenu = aMenu(name = "Deleted Menu", dishesUuids = emptyList(), isDeleted = true)

            menusService.insert(menu1)
            menusService.insert(menu2)
            menusService.insert(deletedMenu)

            val allMenus = menusService.getAll()

            assertEquals(2, allMenus.size, "Should return only non-deleted menus")
            assertTrue(allMenus.none { it.uuid == deletedMenu.uuid }, "Deleted menu should not be in results")
        }

        @Test
        fun `getAll returns empty list when no menus exist`() {
            val allMenus = menusService.getAll()

            assertTrue(allMenus.isEmpty(), "Should return empty list when no menus exist")
        }

        @Test
        fun `getByUuid returns menu with dishes when it exists`() {
            val menu = aMenu(name = "Test Menu", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val retrieved = menusService.getByUuid(menu.uuid)

            assertEquals(menu.uuid, retrieved?.uuid)
            assertEquals(menu.name, retrieved?.name)
            assertEquals(1, retrieved?.dishesUuids?.size)
        }

        @Test
        fun `getByUuid returns null for non-existent UUID`() {
            val nonExistentUuid = UUID.randomUUID()

            val result = menusService.getByUuid(nonExistentUuid)

            assertNull(result, "Should return null for non-existent UUID")
        }

        @Test
        fun `getByUuid returns deleted menu without filtering`() {
            val deletedMenu = aMenu(name = "Deleted Menu", dishesUuids = listOf(testDish1Uuid), isDeleted = true)
            menusService.insert(deletedMenu)

            val retrieved = menusService.getByUuid(deletedMenu.uuid)

            assertEquals(deletedMenu.uuid, retrieved?.uuid, "getByUuid should return deleted menus")
        }
    }

    @Nested
    inner class UpdateOperations {
        @Test
        fun `update returns 1 when menu is successfully updated`() {
            val menu = aMenu(name = "Original Menu", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val updatedMenu =
                UpdatedMenu(
                    name = "Updated Menu",
                    dishesUuids = listOf(testDish2Uuid),
                )

            val updateResult = menusService.update(menu.uuid, updatedMenu)

            assertEquals(1, updateResult, "Update should return 1 for successful update")
        }

        @Test
        fun `update modifies menu name and dishes`() {
            val menu = aMenu(name = "Original Menu", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val updatedMenu =
                UpdatedMenu(
                    name = "Updated Menu",
                    dishesUuids = listOf(testDish2Uuid),
                )
            menusService.update(menu.uuid, updatedMenu)

            val retrieved = menusService.getByUuid(menu.uuid)

            assertEquals("Updated Menu", retrieved?.name)
            assertEquals(1, retrieved?.dishesUuids?.size)
            assertTrue(retrieved?.dishesUuids?.contains(testDish2Uuid) == true)
        }

        @Test
        fun `update can add more dishes to menu`() {
            val menu = aMenu(name = "Menu", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val updatedMenu =
                UpdatedMenu(
                    name = "Menu with more dishes",
                    dishesUuids = listOf(testDish1Uuid, testDish2Uuid),
                )
            menusService.update(menu.uuid, updatedMenu)

            val retrieved = menusService.getByUuid(menu.uuid)

            assertEquals(2, retrieved?.dishesUuids?.size)
        }

        @Test
        fun `update can remove all dishes from menu`() {
            val menu = aMenu(name = "Menu", dishesUuids = listOf(testDish1Uuid, testDish2Uuid))
            menusService.insert(menu)

            val updatedMenu =
                UpdatedMenu(
                    name = "Empty Menu",
                    dishesUuids = emptyList(),
                )
            menusService.update(menu.uuid, updatedMenu)

            val retrieved = menusService.getByUuid(menu.uuid)

            assertTrue(retrieved?.dishesUuids?.isEmpty() == true)
        }

        @Test
        fun `update returns 0 for non-existent menu`() {
            val nonExistentUuid = UUID.randomUUID()
            val updatedMenu =
                UpdatedMenu(
                    name = "Updated Menu",
                    dishesUuids = emptyList(),
                )

            val updateResult = menusService.update(nonExistentUuid, updatedMenu)

            assertEquals(0, updateResult, "Update should return 0 for non-existent menu")
        }
    }

    @Nested
    inner class DeleteOperations {
        @Test
        fun `delete performs soft delete`() {
            val menu = aMenu(name = "Menu to delete", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            menusService.delete(menu.uuid)

            // getByUuid still returns it (no isDeleted filter in getByUuid)
            val retrieved = menusService.getByUuid(menu.uuid)
            assertEquals(menu.uuid, retrieved?.uuid, "Menu should still exist in database")
        }

        @Test
        fun `delete removes menu from getAll results`() {
            val menu = aMenu(name = "Menu to delete", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            menusService.delete(menu.uuid)

            val allMenus = menusService.getAll()
            assertTrue(allMenus.isEmpty(), "Deleted menu should not appear in getAll")
        }

        @Test
        fun `delete returns 0 for non-existent menu`() {
            val nonExistentUuid = UUID.randomUUID()

            val deleteResult = menusService.delete(nonExistentUuid)

            assertEquals(0, deleteResult, "Delete should return 0 for non-existent menu")
        }
    }
}
