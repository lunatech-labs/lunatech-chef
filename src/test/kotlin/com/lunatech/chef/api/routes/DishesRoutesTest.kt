package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.services.DishesService
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
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

class DishesRoutesTest {
    private lateinit var dishesService: DishesService

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        dishesService = DishesService(database)
    }

    private fun ApplicationTestBuilder.setupDishesRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { dishes(dishesService) }
    }

    @Nested
    inner class GetAllDishes {
        @Test
        fun `returns empty list when no dishes exist`() =
            testApplication {
                setupDishesRoutes()

                val response = client.get("/dishes")

                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[]", response.bodyAsText())
            }

        @Test
        fun `returns all non-deleted dishes`() =
            testApplication {
                setupDishesRoutes()
                val dish = aDish(name = "Pasta Carbonara", description = "Classic Italian pasta", hasPork = true)
                dishesService.insert(dish)

                val response = client.get("/dishes")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Pasta Carbonara"))
            }

        @Test
        fun `does not return deleted dishes`() =
            testApplication {
                setupDishesRoutes()
                val activeDish = aDish(name = "Active Dish")
                val deletedDish = aDish(name = "Deleted Dish", isDeleted = true)
                dishesService.insert(activeDish)
                dishesService.insert(deletedDish)

                val response = client.get("/dishes")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Active Dish"))
                assertFalse(response.bodyAsText().contains("Deleted Dish"))
            }
    }

    @Nested
    inner class GetDishByUuid {
        @Test
        fun `returns dish when it exists`() =
            testApplication {
                setupDishesRoutes()
                val dish = aDish(name = "Pasta", description = "Italian pasta")
                dishesService.insert(dish)

                val response = client.get("/dishes/${dish.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Pasta"))
            }

        @Test
        fun `returns NotFound for non-existent UUID`() =
            testApplication {
                setupDishesRoutes()

                val response = client.get("/dishes/${UUID.randomUUID()}")

                assertEquals(HttpStatusCode.NotFound, response.status)
            }
    }

    @Nested
    inner class CreateDish {
        @Test
        fun `creates new dish with minimal properties`() =
            testApplication {
                setupDishesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/dishes") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "name" to "Caesar Salad",
                                "description" to "Fresh salad with croutons",
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
                val dishes = dishesService.getAll()
                assertEquals(1, dishes.size)
                assertEquals("Caesar Salad", dishes[0].name)
            }

        @Test
        fun `creates vegetarian dish`() =
            testApplication {
                setupDishesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/dishes") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "name" to "Garden Salad",
                                "description" to "Fresh vegetables",
                                "isVegetarian" to true,
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
                val dishes = dishesService.getAll()
                assertTrue(dishes[0].isVegetarian)
            }

        @Test
        fun `creates dish with all dietary flags`() =
            testApplication {
                setupDishesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/dishes") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "name" to "Special Dish",
                                "description" to "All flags set",
                                "isVegetarian" to true,
                                "isHalal" to true,
                                "hasNuts" to true,
                                "hasSeafood" to true,
                                "hasPork" to true,
                                "hasBeef" to true,
                                "isGlutenFree" to true,
                                "isLactoseFree" to true,
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
                val dishes = dishesService.getAll()
                assertEquals(1, dishes.size)
                assertTrue(dishes[0].isVegetarian)
                assertTrue(dishes[0].isHalal)
                assertTrue(dishes[0].hasNuts)
                assertTrue(dishes[0].hasSeafood)
                assertTrue(dishes[0].hasPork)
                assertTrue(dishes[0].hasBeef)
                assertTrue(dishes[0].isGlutenFree)
                assertTrue(dishes[0].isLactoseFree)
            }

        @Test
        fun `returns BadRequest for invalid JSON`() =
            testApplication {
                setupDishesRoutes()
                val client = jsonClient()

                val response =
                    client.post("/dishes") {
                        contentType(ContentType.Application.Json)
                        setBody("{ invalid json }")
                    }

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }
    }

    @Nested
    inner class UpdateDish {
        @Test
        fun `updates existing dish`() =
            testApplication {
                setupDishesRoutes()
                val client = jsonClient()
                val dish = aDish(name = "Salad", description = "Fresh salad", isVegetarian = true)
                dishesService.insert(dish)

                val response =
                    client.put("/dishes/${dish.uuid}") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "name" to "Caesar Salad",
                                "description" to "With croutons",
                                "isVegetarian" to false,
                                "isHalal" to false,
                                "hasNuts" to false,
                                "hasSeafood" to false,
                                "hasPork" to false,
                                "hasBeef" to false,
                                "isGlutenFree" to false,
                                "isLactoseFree" to false,
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                val updated = dishesService.getByUuid(dish.uuid)
                assertEquals("Caesar Salad", updated[0].name)
                assertEquals("With croutons", updated[0].description)
                assertFalse(updated[0].isVegetarian)
            }
    }

    @Nested
    inner class DeleteDish {
        @Test
        fun `soft deletes dish`() =
            testApplication {
                setupDishesRoutes()
                val dish = aDish(name = "Pasta", description = "Italian pasta")
                dishesService.insert(dish)

                val response = client.delete("/dishes/${dish.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
                val dishes = dishesService.getAll()
                assertTrue(dishes.isEmpty(), "Deleted dish should not appear in getAll")
            }
    }
}
