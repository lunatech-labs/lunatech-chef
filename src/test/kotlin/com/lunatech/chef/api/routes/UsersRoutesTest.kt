package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.UsersService
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

class UsersRoutesTest {
    private lateinit var usersService: UsersService
    private lateinit var officesService: OfficesService
    private lateinit var testOfficeUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        usersService = UsersService(database)
        officesService = OfficesService(database)

        val testOffice = anOffice(city = "Rotterdam")
        officesService.insert(testOffice)
        testOfficeUuid = testOffice.uuid
    }

    private fun ApplicationTestBuilder.setupUsersRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { users(usersService) }
    }

    @Nested
    inner class GetAllUsers {
        @Test
        fun `returns empty list when no users exist`() =
            testApplication {
                setupUsersRoutes()

                val response = client.get("/users")

                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[]", response.bodyAsText())
            }

        @Test
        fun `returns all non-deleted users`() =
            testApplication {
                setupUsersRoutes()
                val user = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
                usersService.insert(user)

                val response = client.get("/users")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("John Doe"))
            }

        @Test
        fun `does not return deleted users`() =
            testApplication {
                setupUsersRoutes()
                val activeUser = aUser(name = "Active User", emailAddress = uniqueEmail("active"), officeUuid = testOfficeUuid)
                val deletedUser =
                    aUser(name = "Deleted User", emailAddress = uniqueEmail("deleted"), officeUuid = testOfficeUuid, isDeleted = true)
                usersService.insert(activeUser)
                usersService.insert(deletedUser)

                val response = client.get("/users")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("Active User"))
                assertFalse(response.bodyAsText().contains("Deleted User"))
            }
    }

    @Nested
    inner class GetUserByUuid {
        @Test
        fun `returns user when it exists`() =
            testApplication {
                setupUsersRoutes()
                val user = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
                usersService.insert(user)

                val response = client.get("/users/${user.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains("John Doe"))
            }

        @Test
        fun `returns NotFound for non-existent UUID`() =
            testApplication {
                setupUsersRoutes()

                val response = client.get("/users/${UUID.randomUUID()}")

                assertEquals(HttpStatusCode.NotFound, response.status)
            }
    }

    @Nested
    inner class CreateUser {
        @Test
        fun `creates new user`() =
            testApplication {
                setupUsersRoutes()
                val client = jsonClient()

                val response =
                    client.post("/users") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "name" to "Jane Doe",
                                "emailAddress" to "jane@lunatech.nl",
                                "officeUuid" to testOfficeUuid.toString(),
                                "isVegetarian" to true,
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
                val users = usersService.getAll()
                assertEquals(1, users.size)
                assertEquals("Jane Doe", users[0].name)
            }

        @Test
        fun `creates user with null office`() =
            testApplication {
                setupUsersRoutes()
                val client = jsonClient()

                val response =
                    client.post("/users") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "name" to "No Office User",
                                "emailAddress" to "nooffice@lunatech.nl",
                                "officeUuid" to null,
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.Created, response.status)
            }

        @Test
        fun `returns BadRequest for invalid JSON`() =
            testApplication {
                setupUsersRoutes()
                val client = jsonClient()

                val response =
                    client.post("/users") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody("{ invalid json }")
                    }

                assertEquals(HttpStatusCode.BadRequest, response.status)
            }
    }

    @Nested
    inner class UpdateUser {
        @Test
        fun `updates existing user`() =
            testApplication {
                setupUsersRoutes()
                val client = jsonClient()
                val user = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid, isVegetarian = false)
                usersService.insert(user)

                val response =
                    client.put("/users/${user.uuid}") {
                        contentType(RouteTestHelpers.jsonContentType)
                        setBody(
                            mapOf(
                                "officeUuid" to testOfficeUuid.toString(),
                                "isVegetarian" to true,
                                "hasHalalRestriction" to true,
                                "hasNutsRestriction" to false,
                                "hasSeafoodRestriction" to false,
                                "hasPorkRestriction" to false,
                                "hasBeefRestriction" to false,
                                "isGlutenIntolerant" to false,
                                "isLactoseIntolerant" to false,
                                "otherRestrictions" to "No spicy food",
                            ),
                        )
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                val updated = usersService.getByUuid(user.uuid)
                assertTrue(updated[0].isVegetarian)
                assertTrue(updated[0].hasHalalRestriction)
                assertEquals("No spicy food", updated[0].otherRestrictions)
            }
    }

    @Nested
    inner class DeleteUser {
        @Test
        fun `soft deletes user`() =
            testApplication {
                setupUsersRoutes()
                val user = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
                usersService.insert(user)

                val response = client.delete("/users/${user.uuid}")

                assertEquals(HttpStatusCode.OK, response.status)
                val users = usersService.getAll()
                assertTrue(users.isEmpty(), "Deleted user should not appear in getAll")
            }
    }
}
