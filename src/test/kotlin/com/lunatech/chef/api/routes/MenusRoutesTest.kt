package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class MenusRoutesTest {
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

        val testDish1 = aDish(name = "Pasta", description = "Italian pasta", isVegetarian = true)
        val testDish2 = aDish(name = "Salad", description = "Fresh salad", isVegetarian = true)
        dishesService.insert(testDish1)
        dishesService.insert(testDish2)
        testDish1Uuid = testDish1.uuid
        testDish2Uuid = testDish2.uuid
    }

    private fun ApplicationTestBuilder.setupMenusRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { menus(menusService) }
    }

    @Nested
    inner class GetAllMenus {
        @Test
        fun `returns empty list when no menus exist`() = testApplication {
            setupMenusRoutes()

            val response = client.get("/menus")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

        @Test
        fun `returns all non-deleted menus`() = testApplication {
            setupMenusRoutes()
            val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val response = client.get("/menus")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Lunch Menu"))
        }

        @Test
        fun `does not return deleted menus`() = testApplication {
            setupMenusRoutes()
            val activeMenu = aMenu(name = "Active Menu", dishesUuids = listOf(testDish1Uuid))
            val deletedMenu = aMenu(name = "Deleted Menu", dishesUuids = listOf(testDish1Uuid), isDeleted = true)
            menusService.insert(activeMenu)
            menusService.insert(deletedMenu)

            val response = client.get("/menus")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Active Menu"))
            assertFalse(response.bodyAsText().contains("Deleted Menu"))
        }
    }

    @Nested
    inner class GetMenuByUuid {
        @Test
        fun `returns menu when it exists`() = testApplication {
            setupMenusRoutes()
            val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val response = client.get("/menus/${menu.uuid}")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Lunch Menu"))
        }

        @Test
        fun `returns NotFound for non-existent UUID`() = testApplication {
            setupMenusRoutes()

            val response = client.get("/menus/${UUID.randomUUID()}")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    }

    @Nested
    inner class CreateMenu {
        @Test
        fun `creates new menu with dishes`() = testApplication {
            setupMenusRoutes()
            val client = jsonClient()

            val response = client.post("/menus") {
                contentType(RouteTestHelpers.jsonContentType)
                setBody(mapOf(
                    "name" to "Dinner Menu",
                    "dishesUuids" to listOf(testDish1Uuid.toString(), testDish2Uuid.toString())
                ))
            }

            assertEquals(HttpStatusCode.Created, response.status)
            val menus = menusService.getAll()
            assertEquals(1, menus.size)
            assertEquals("Dinner Menu", menus[0].name)
        }

        @Test
        fun `creates menu with empty dishes`() = testApplication {
            setupMenusRoutes()
            val client = jsonClient()

            val response = client.post("/menus") {
                contentType(RouteTestHelpers.jsonContentType)
                setBody(mapOf(
                    "name" to "Empty Menu",
                    "dishesUuids" to emptyList<String>()
                ))
            }

            assertEquals(HttpStatusCode.Created, response.status)
        }

        @Test
        fun `returns BadRequest for invalid JSON`() = testApplication {
            setupMenusRoutes()
            val client = jsonClient()

            val response = client.post("/menus") {
                contentType(RouteTestHelpers.jsonContentType)
                setBody("{ invalid json }")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Nested
    inner class UpdateMenu {
        @Test
        fun `updates existing menu`() = testApplication {
            setupMenusRoutes()
            val client = jsonClient()
            val menu = aMenu(name = "Original Menu", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val response = client.put("/menus/${menu.uuid}") {
                contentType(RouteTestHelpers.jsonContentType)
                setBody(mapOf(
                    "name" to "Updated Menu",
                    "dishesUuids" to listOf(testDish2Uuid.toString())
                ))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val updated = menusService.getByUuid(menu.uuid)
            assertEquals("Updated Menu", updated?.name)
        }

    }

    @Nested
    inner class DeleteMenu {
        @Test
        fun `soft deletes menu`() = testApplication {
            setupMenusRoutes()
            val menu = aMenu(name = "Menu to delete", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val response = client.delete("/menus/${menu.uuid}")

            assertEquals(HttpStatusCode.OK, response.status)
            val menus = menusService.getAll()
            assertTrue(menus.isEmpty(), "Deleted menu should not appear in getAll")
        }

    }
}
