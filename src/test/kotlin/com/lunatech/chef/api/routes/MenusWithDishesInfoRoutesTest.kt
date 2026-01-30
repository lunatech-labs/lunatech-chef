package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
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

class MenusWithDishesInfoRoutesTest {
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

        val testDish1 = aDish(name = "Pasta Carbonara", description = "Classic Italian pasta", hasPork = true)
        val testDish2 = aDish(name = "Caesar Salad", description = "Fresh salad with croutons", isVegetarian = true)
        dishesService.insert(testDish1)
        dishesService.insert(testDish2)
        testDish1Uuid = testDish1.uuid
        testDish2Uuid = testDish2.uuid
    }

    private fun ApplicationTestBuilder.setupMenusWithDishesInfoRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { menusWithDishesInfo(menusWithDishesNamesService) }
    }

    @Nested
    inner class GetAllMenusWithDishesInfo {
        @Test
        fun `returns empty list when no menus exist`() = testApplication {
            setupMenusWithDishesInfoRoutes()

            val response = client.get("/menusWithDishesInfo")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

        @Test
        fun `returns menus with full dish details`() = testApplication {
            setupMenusWithDishesInfoRoutes()
            val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(testDish1Uuid, testDish2Uuid))
            menusService.insert(menu)

            val response = client.get("/menusWithDishesInfo")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Lunch Menu"))
            assertTrue(response.bodyAsText().contains("Pasta Carbonara"))
            assertTrue(response.bodyAsText().contains("Caesar Salad"))
        }

        @Test
        fun `does not return deleted menus`() = testApplication {
            setupMenusWithDishesInfoRoutes()
            val activeMenu = aMenu(name = "Active Menu", dishesUuids = listOf(testDish1Uuid))
            val deletedMenu = aMenu(name = "Deleted Menu", dishesUuids = listOf(testDish1Uuid), isDeleted = true)
            menusService.insert(activeMenu)
            menusService.insert(deletedMenu)

            val response = client.get("/menusWithDishesInfo")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Active Menu"))
            assertFalse(response.bodyAsText().contains("Deleted Menu"))
        }
    }

    @Nested
    inner class GetMenuWithDishesInfoByUuid {
        @Test
        fun `returns menu with dish details`() = testApplication {
            setupMenusWithDishesInfoRoutes()
            val menu = aMenu(name = "Special Menu", dishesUuids = listOf(testDish1Uuid))
            menusService.insert(menu)

            val response = client.get("/menusWithDishesInfo/${menu.uuid}")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Special Menu"))
            assertTrue(response.bodyAsText().contains("Pasta Carbonara"))
        }

        @Test
        fun `returns NotFound for non-existent menu`() = testApplication {
            setupMenusWithDishesInfoRoutes()

            val response = client.get("/menusWithDishesInfo/${UUID.randomUUID()}")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    }
}
