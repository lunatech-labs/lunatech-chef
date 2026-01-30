package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.services.OfficesService
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

class OfficesRoutesTest {
    private lateinit var officesService: OfficesService

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        officesService = OfficesService(database)
    }

    private fun ApplicationTestBuilder.setupOfficesRoutes() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        routing { offices(officesService) }
    }

    @Nested
    inner class GetAllOffices {
        @Test
        fun `returns empty list when no offices exist`() = testApplication {
            setupOfficesRoutes()

            val response = client.get("/offices")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

        @Test
        fun `returns all non-deleted offices`() = testApplication {
            setupOfficesRoutes()
            val office = anOffice(city = "Rotterdam", country = "Netherlands")
            officesService.insert(office)

            val response = client.get("/offices")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Rotterdam"))
            assertTrue(response.bodyAsText().contains("Netherlands"))
        }

        @Test
        fun `does not return deleted offices`() = testApplication {
            setupOfficesRoutes()
            val activeOffice = anOffice(city = "Rotterdam")
            val deletedOffice = anOffice(city = "Deleted City", isDeleted = true)
            officesService.insert(activeOffice)
            officesService.insert(deletedOffice)

            val response = client.get("/offices")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Rotterdam"))
            assertFalse(response.bodyAsText().contains("Deleted City"))
        }
    }

    @Nested
    inner class GetOfficeByUuid {
        @Test
        fun `returns office when it exists`() = testApplication {
            setupOfficesRoutes()
            val office = anOffice(city = "Rotterdam", country = "Netherlands")
            officesService.insert(office)

            val response = client.get("/offices/${office.uuid}")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Rotterdam"))
        }

        @Test
        fun `returns NotFound for non-existent UUID`() = testApplication {
            setupOfficesRoutes()

            val response = client.get("/offices/${UUID.randomUUID()}")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    }

    @Nested
    inner class CreateOffice {
        @Test
        fun `creates new office`() = testApplication {
            setupOfficesRoutes()
            val client = jsonClient()

            val response = client.post("/offices") {
                contentType(RouteTestHelpers.jsonContentType)
                setBody(mapOf("city" to "Paris", "country" to "France"))
            }

            assertEquals(HttpStatusCode.Created, response.status)
            val offices = officesService.getAll()
            assertEquals(1, offices.size)
            assertEquals("Paris", offices[0].city)
            assertEquals("France", offices[0].country)
        }

        @Test
        fun `returns BadRequest for invalid JSON`() = testApplication {
            setupOfficesRoutes()
            val client = jsonClient()

            val response = client.post("/offices") {
                contentType(RouteTestHelpers.jsonContentType)
                setBody("{ invalid json }")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Nested
    inner class UpdateOffice {
        @Test
        fun `updates existing office`() = testApplication {
            setupOfficesRoutes()
            val client = jsonClient()
            val office = anOffice(city = "Rotterdam", country = "Netherlands")
            officesService.insert(office)

            val response = client.put("/offices/${office.uuid}") {
                contentType(RouteTestHelpers.jsonContentType)
                setBody(mapOf("city" to "Amsterdam", "country" to "Netherlands"))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val updated = officesService.getByUuid(office.uuid)
            assertEquals("Amsterdam", updated[0].city)
        }

    }

    @Nested
    inner class DeleteOffice {
        @Test
        fun `soft deletes office`() = testApplication {
            setupOfficesRoutes()
            val office = anOffice(city = "Rotterdam", country = "Netherlands")
            officesService.insert(office)

            val response = client.delete("/offices/${office.uuid}")

            assertEquals(HttpStatusCode.OK, response.status)
            val offices = officesService.getAll()
            assertTrue(offices.isEmpty(), "Deleted office should not appear in getAll")
        }

    }
}
