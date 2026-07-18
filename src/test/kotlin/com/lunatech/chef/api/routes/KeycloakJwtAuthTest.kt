package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.KEYCLOAK_AUTH
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Exercises the shared keycloakJwt configuration end to end: which tokens
 * pass verification and which are rejected with a 401 challenge.
 */
class KeycloakJwtAuthTest {
    private lateinit var usersService: UsersService

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        usersService = UsersService(database)
    }

    private fun ApplicationTestBuilder.setupProtectedRoute() {
        installKeycloakAuth(usersService)
        routing {
            authenticate(KEYCLOAK_AUTH) {
                get("/protected") { call.respondText("ok") }
            }
        }
    }

    private suspend fun ApplicationTestBuilder.statusFor(token: String?): HttpStatusCode =
        client
            .get("/protected") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
            }.status

    @Test
    fun `accepts a valid token`() =
        testApplication {
            setupProtectedRoute()
            assertEquals(HttpStatusCode.OK, statusFor(accessTokenFor(uniqueEmail("ok"))))
        }

    @Test
    fun `rejects a request without a token`() =
        testApplication {
            setupProtectedRoute()
            assertEquals(HttpStatusCode.Unauthorized, statusFor(null))
        }

    @Test
    fun `rejects an expired token`() =
        testApplication {
            setupProtectedRoute()
            val expired = accessTokenFor(uniqueEmail("late"), expiresAt = Instant.now().minusSeconds(60))
            assertEquals(HttpStatusCode.Unauthorized, statusFor(expired))
        }

    @Test
    fun `rejects a token for another client`() =
        testApplication {
            setupProtectedRoute()
            assertEquals(HttpStatusCode.Unauthorized, statusFor(accessTokenFor(uniqueEmail("other"), audience = "other-client")))
        }

    @Test
    fun `rejects a token without an audience`() =
        testApplication {
            setupProtectedRoute()
            assertEquals(HttpStatusCode.Unauthorized, statusFor(accessTokenFor(uniqueEmail("noaud"), audience = null)))
        }

    @Test
    fun `rejects a token from another issuer`() =
        testApplication {
            setupProtectedRoute()
            assertEquals(HttpStatusCode.Unauthorized, statusFor(accessTokenFor(uniqueEmail("rogue"), issuer = "https://evil.example")))
        }

    @Test
    fun `rejects a token whose email is not verified`() =
        testApplication {
            setupProtectedRoute()
            assertEquals(HttpStatusCode.Unauthorized, statusFor(accessTokenFor(uniqueEmail("shady"), emailVerified = false)))
        }
}
