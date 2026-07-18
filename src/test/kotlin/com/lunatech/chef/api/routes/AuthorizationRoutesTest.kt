package com.lunatech.chef.api.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import com.lunatech.chef.api.auth.ADMIN_ROLE
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.header
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

private const val TEST_JWT_SECRET = "test-jwt-secret"
private const val TEST_CLIENT_ID = "lunachef-test"

class AuthorizationRoutesTest {
    @Nested
    inner class GetUserNameFromEmailTests {
        @Test
        fun `extracts and formats name correctly`() {
            val email = "john.doe@lunatech.nl"

            val name = getUserNameFromEmail(email)

            assertEquals("John Doe", name)
        }

        @Test
        fun `handles single name`() {
            val email = "admin@lunatech.nl"

            val name = getUserNameFromEmail(email)

            assertEquals("Admin", name)
        }

        @Test
        fun `handles multiple parts`() {
            val email = "john.middle.doe@lunatech.nl"

            val name = getUserNameFromEmail(email)

            assertEquals("John Middle Doe", name)
        }
    }

    @Nested
    inner class ExtractRolesTests {
        private fun payloadOf(token: String): Payload = JWT.decode(token)

        @Test
        fun `returns roles when claim contains admin`() {
            val token =
                JWT
                    .create()
                    .withClaim("roles", listOf(ADMIN_ROLE, "user"))
                    .sign(Algorithm.HMAC256(TEST_JWT_SECRET))

            val roles = extractRoles(payloadOf(token))

            assertEquals(listOf(ADMIN_ROLE, "user"), roles)
        }

        @Test
        fun `returns roles when claim does not contain admin`() {
            val token =
                JWT
                    .create()
                    .withClaim("roles", listOf("user"))
                    .sign(Algorithm.HMAC256(TEST_JWT_SECRET))

            val roles = extractRoles(payloadOf(token))

            assertEquals(listOf("user"), roles)
        }

        @Test
        fun `returns empty list when claim is absent`() {
            val token =
                JWT
                    .create()
                    .withClaim("email", "user@lunatech.nl")
                    .sign(Algorithm.HMAC256(TEST_JWT_SECRET))

            val roles = extractRoles(payloadOf(token))

            assertEquals(emptyList<String>(), roles)
        }

        @Test
        fun `returns empty list when claim has the wrong type`() {
            val token =
                JWT
                    .create()
                    .withClaim("roles", ADMIN_ROLE)
                    .sign(Algorithm.HMAC256(TEST_JWT_SECRET))

            val roles = extractRoles(payloadOf(token))

            assertEquals(emptyList<String>(), roles)
        }
    }

    @Nested
    inner class BuildChefSessionTests {
        @Test
        fun `creates session with correct properties`() {
            val officeUuid = UUID.randomUUID()
            val user =
                aUser(
                    name = "John Doe",
                    emailAddress = "john@lunatech.nl",
                    officeUuid = officeUuid,
                    isVegetarian = true,
                    hasNutsRestriction = true,
                    isLactoseIntolerant = true,
                    otherRestrictions = "No spicy food",
                )

            val session = buildChefSession(user, listOf(ADMIN_ROLE))

            assertEquals(user.uuid, session.uuid)
            assertEquals(user.name, session.name)
            assertEquals(user.emailAddress, session.emailAddress)
            assertEquals(officeUuid.toString(), session.officeUuid)
            assertTrue(session.isAdmin)
            assertTrue(session.isVegetarian)
            assertTrue(session.hasNutsRestriction)
            assertTrue(session.isLactoseIntolerant)
            assertEquals("No spicy food", session.otherRestrictions)
            assertNotNull(session.ttl)
        }

        @Test
        fun `sets isAdmin false without the admin role`() {
            val user =
                aUser(
                    name = "Jane Doe",
                    emailAddress = "jane@lunatech.nl",
                    officeUuid = null,
                )

            val session = buildChefSession(user, listOf("user"))

            assertFalse(session.isAdmin)
            assertEquals("", session.officeUuid)
        }

        @Test
        fun `sets isAdmin false for empty roles`() {
            val user =
                aUser(
                    name = "Jane Doe",
                    emailAddress = "jane@lunatech.nl",
                    officeUuid = null,
                )

            val session = buildChefSession(user, emptyList())

            assertFalse(session.isAdmin)
        }

        @Test
        fun `sets empty officeUuid for user without office`() {
            val user =
                aUser(
                    name = "Test User",
                    emailAddress = "test@lunatech.nl",
                    officeUuid = null,
                )

            val session = buildChefSession(user, emptyList())

            assertEquals("", session.officeUuid)
        }

        @Test
        fun `preserves all dietary restrictions`() {
            val user =
                aUser(
                    name = "Dietary User",
                    emailAddress = "diet@lunatech.nl",
                    officeUuid = null,
                    isVegetarian = true,
                    hasHalalRestriction = true,
                    hasNutsRestriction = true,
                    hasSeafoodRestriction = true,
                    hasPorkRestriction = true,
                    hasBeefRestriction = true,
                    isGlutenIntolerant = true,
                    isLactoseIntolerant = true,
                )

            val session = buildChefSession(user, emptyList())

            assertTrue(session.isVegetarian)
            assertTrue(session.hasHalalRestriction)
            assertTrue(session.hasNutsRestriction)
            assertTrue(session.hasSeafoodRestriction)
            assertTrue(session.hasPorkRestriction)
            assertTrue(session.hasBeefRestriction)
            assertTrue(session.isGlutenIntolerant)
            assertTrue(session.isLactoseIntolerant)
        }
    }

    @Nested
    inner class LoginRouteTests {
        private lateinit var schedulesService: SchedulesService
        private lateinit var attendancesService: AttendancesService
        private lateinit var usersService: UsersService

        @BeforeEach
        fun setup() {
            val database = TestDatabase.getDatabase()
            TestDatabase.resetDatabase()
            schedulesService = SchedulesService(database)
            usersService = UsersService(database)
            attendancesService = AttendancesService(database, usersService)
        }

        private fun ApplicationTestBuilder.setupLoginRoute() {
            install(ContentNegotiation) {
                register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
            }
            install(Sessions) {
                header<ChefSession>(TEST_SESSION_HEADER) {
                    transform(SessionTransportTransformerMessageAuthentication("test-session-secret".encodeToByteArray()))
                }
            }
            install(Authentication) {
                jwt("idtoken") {
                    // Mirrors production: the verifier requires the token audience to
                    // be the configured client id, on top of the signature check.
                    verifier(
                        JWT
                            .require(Algorithm.HMAC256(TEST_JWT_SECRET))
                            .withAudience(TEST_CLIENT_ID)
                            .build(),
                    )
                    validate { credential -> JWTPrincipal(credential.payload) }
                }
            }
            routing {
                authentication(schedulesService, attendancesService, usersService)
            }
        }

        private fun idTokenFor(
            email: String,
            roles: List<String>? = null,
            audience: String? = TEST_CLIENT_ID,
        ): String {
            val builder = JWT.create().withClaim("email", email)
            if (roles != null) builder.withClaim("roles", roles)
            if (audience != null) builder.withAudience(audience)
            return builder.sign(Algorithm.HMAC256(TEST_JWT_SECRET))
        }

        @Test
        fun `login with the admin role yields an admin session`() =
            testApplication {
                setupLoginRoute()
                val client = jsonClient()

                val response =
                    client.get("/login") {
                        header(HttpHeaders.Authorization, "Bearer ${idTokenFor(uniqueEmail("admin"), listOf(ADMIN_ROLE))}")
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                val session = response.body<ChefSession>()
                assertTrue(session.isAdmin)
            }

        @Test
        fun `login without a roles claim yields a non-admin session`() =
            testApplication {
                setupLoginRoute()
                val client = jsonClient()

                val response =
                    client.get("/login") {
                        header(HttpHeaders.Authorization, "Bearer ${idTokenFor(uniqueEmail("employee"))}")
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                val session = response.body<ChefSession>()
                assertFalse(session.isAdmin)
            }

        @Test
        fun `login with a token for another client is unauthorized`() =
            testApplication {
                setupLoginRoute()
                val client = jsonClient()

                val response =
                    client.get("/login") {
                        header(
                            HttpHeaders.Authorization,
                            "Bearer ${idTokenFor(uniqueEmail("intruder"), listOf(ADMIN_ROLE), audience = "other-client")}",
                        )
                    }

                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        @Test
        fun `login with a token without an audience is unauthorized`() =
            testApplication {
                setupLoginRoute()
                val client = jsonClient()

                val response =
                    client.get("/login") {
                        header(
                            HttpHeaders.Authorization,
                            "Bearer ${idTokenFor(uniqueEmail("intruder"), listOf(ADMIN_ROLE), audience = null)}",
                        )
                    }

                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        @Test
        fun `login with roles missing admin yields a non-admin session`() =
            testApplication {
                setupLoginRoute()
                val client = jsonClient()

                val response =
                    client.get("/login") {
                        header(HttpHeaders.Authorization, "Bearer ${idTokenFor(uniqueEmail("employee"), listOf("user"))}")
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                val session = response.body<ChefSession>()
                assertFalse(session.isAdmin)
            }
    }

    @Nested
    inner class ValidateSessionTests {
        @Test
        fun `returns principal for valid session`() {
            val formatDate = SimpleDateFormat("yyMMddHHmmss")
            val session =
                ChefSession(
                    ttl = formatDate.format(Date()),
                    isAdmin = false,
                    uuid = UUID.randomUUID(),
                    name = "Test User",
                    emailAddress = "test@lunatech.nl",
                    officeUuid = "",
                )

            val principal = validateSession(session, 60)

            assertNotNull(principal)
            assertEquals("test@lunatech.nl", principal?.emailAddress)
        }

        @Test
        fun `returns null for expired session`() {
            val session =
                ChefSession(
                    ttl = "200101000000", // Very old timestamp (2020-01-01)
                    isAdmin = false,
                    uuid = UUID.randomUUID(),
                    name = "Test User",
                    emailAddress = "test@lunatech.nl",
                    officeUuid = "",
                )

            val principal = validateSession(session, 60)

            assertNull(principal)
        }

        @Test
        fun `returns null for invalid ttl format`() {
            val session =
                ChefSession(
                    ttl = "invalid",
                    isAdmin = false,
                    uuid = UUID.randomUUID(),
                    name = "Test User",
                    emailAddress = "test@lunatech.nl",
                    officeUuid = "",
                )

            val principal = validateSession(session, 60)

            assertNull(principal)
        }

        @Test
        fun `returns null for empty ttl`() {
            val session =
                ChefSession(
                    ttl = "",
                    isAdmin = false,
                    uuid = UUID.randomUUID(),
                    name = "Test User",
                    emailAddress = "test@lunatech.nl",
                    officeUuid = "",
                )

            val principal = validateSession(session, 60)

            assertNull(principal)
        }
    }
}
