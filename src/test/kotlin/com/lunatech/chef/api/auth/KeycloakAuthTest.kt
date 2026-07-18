package com.lunatech.chef.api.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.services.UsersService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val TEST_JWT_SECRET = "test-jwt-secret"

class KeycloakAuthTest {
    private lateinit var usersService: UsersService

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        usersService = UsersService(database)
    }

    private fun payloadOf(
        email: String? = null,
        emailVerified: Boolean? = true,
        roles: List<String>? = emptyList(),
    ): Payload {
        val builder = JWT.create()
        if (email != null) builder.withClaim("email", email)
        if (emailVerified != null) builder.withClaim("email_verified", emailVerified)
        if (roles != null) builder.withClaim("roles", roles)
        return JWT.decode(builder.sign(Algorithm.HMAC256(TEST_JWT_SECRET)))
    }

    @Nested
    inner class ValidateKeycloakToken {
        @Test
        fun `resolves the user for a known email`() {
            val user = aUser(name = "Known User", emailAddress = uniqueEmail("known"))
            usersService.insert(user)

            val principal = validateKeycloakToken(payloadOf(email = user.emailAddress), usersService)

            assertNotNull(principal)
            assertEquals(user.uuid, principal?.user?.uuid)
            assertEquals(user.emailAddress, principal?.email)
        }

        @Test
        fun `yields a principal without a user for an unknown email`() {
            val principal = validateKeycloakToken(payloadOf(email = uniqueEmail("unknown")), usersService)

            assertNotNull(principal)
            assertNull(principal?.user)
        }

        @Test
        fun `sets isAdmin when the roles claim contains the admin role`() {
            val principal = validateKeycloakToken(payloadOf(email = uniqueEmail("boss"), roles = listOf(ADMIN_ROLE)), usersService)

            assertTrue(principal!!.isAdmin)
        }

        @Test
        fun `sets isAdmin false without the admin role`() {
            val principal = validateKeycloakToken(payloadOf(email = uniqueEmail("emp"), roles = listOf("user")), usersService)

            assertFalse(principal!!.isAdmin)
        }

        @Test
        fun `rejects a token whose email is not verified`() {
            assertNull(validateKeycloakToken(payloadOf(email = uniqueEmail("shady"), emailVerified = false), usersService))
        }

        @Test
        fun `rejects a token without an email_verified claim`() {
            assertNull(validateKeycloakToken(payloadOf(email = uniqueEmail("shady"), emailVerified = null), usersService))
        }

        @Test
        fun `rejects a token without an email claim`() {
            assertNull(validateKeycloakToken(payloadOf(email = null), usersService))
        }
    }

    @Nested
    inner class ExtractRoles {
        @Test
        fun `returns roles when claim contains admin`() {
            assertEquals(listOf(ADMIN_ROLE, "user"), extractRoles(payloadOf(email = "x@lunatech.nl", roles = listOf(ADMIN_ROLE, "user"))))
        }

        @Test
        fun `returns empty list when claim is absent`() {
            assertEquals(emptyList<String>(), extractRoles(payloadOf(email = "x@lunatech.nl", roles = null)))
        }

        @Test
        fun `returns empty list when claim has the wrong type`() {
            val token = JWT.create().withClaim("roles", "admin").sign(Algorithm.HMAC256(TEST_JWT_SECRET))
            assertEquals(emptyList<String>(), extractRoles(JWT.decode(token)))
        }
    }
}
