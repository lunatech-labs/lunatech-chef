package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.TestFixtures.aUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

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
    inner class IsAdminTests {
        @Test
        fun `returns true for admin email`() {
            val admins = listOf("admin@lunatech.nl", "superadmin@lunatech.nl")

            val result = isAdmin(admins, "admin@lunatech.nl")

            assertTrue(result)
        }

        @Test
        fun `returns false for non-admin email`() {
            val admins = listOf("admin@lunatech.nl")

            val result = isAdmin(admins, "user@lunatech.nl")

            assertFalse(result)
        }

        @Test
        fun `returns false for empty admin list`() {
            val admins = emptyList<String>()

            val result = isAdmin(admins, "user@lunatech.nl")

            assertFalse(result)
        }

        @Test
        fun `is case sensitive`() {
            val admins = listOf("Admin@lunatech.nl")

            val result = isAdmin(admins, "admin@lunatech.nl")

            assertFalse(result)
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
            val admins = listOf("john@lunatech.nl")

            val session = buildChefSession(user, admins)

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
        fun `sets isAdmin false for non-admin`() {
            val user =
                aUser(
                    name = "Jane Doe",
                    emailAddress = "jane@lunatech.nl",
                    officeUuid = null,
                )
            val admins = listOf("admin@lunatech.nl")

            val session = buildChefSession(user, admins)

            assertFalse(session.isAdmin)
            assertEquals("", session.officeUuid)
        }

        @Test
        fun `sets empty officeUuid for user without office`() {
            val user =
                aUser(
                    name = "Test User",
                    emailAddress = "test@lunatech.nl",
                    officeUuid = null,
                )
            val admins = emptyList<String>()

            val session = buildChefSession(user, admins)

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
            val admins = emptyList<String>()

            val session = buildChefSession(user, admins)

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
            assertEquals("test@lunatech.nl", principal?.email)
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
