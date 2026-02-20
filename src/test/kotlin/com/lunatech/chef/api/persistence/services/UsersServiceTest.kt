package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.routes.UpdatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class UsersServiceTest {
    private lateinit var usersService: UsersService
    private lateinit var officesService: OfficesService
    private lateinit var testOfficeUuid: UUID

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        usersService = UsersService(database)
        officesService = OfficesService(database)

        // Create a test office for user foreign key
        val testOffice = anOffice(city = "Rotterdam")
        officesService.insert(testOffice)
        testOfficeUuid = testOffice.uuid
    }

    @Nested
    inner class InsertOperations {
        @Test
        fun `insert returns 1 when user is successfully created`() {
            val user =
                aUser(
                    name = "John Doe",
                    emailAddress = uniqueEmail("john"),
                    officeUuid = testOfficeUuid,
                )

            val insertResult = usersService.insert(user)

            assertEquals(1, insertResult, "Insert should return 1 for successful creation")
        }

        @Test
        fun `insert persists all user properties correctly`() {
            val user =
                aUser(
                    name = "John Doe",
                    emailAddress = uniqueEmail("john"),
                    officeUuid = testOfficeUuid,
                    isVegetarian = true,
                )

            usersService.insert(user)
            val retrieved = usersService.getByUuid(user.uuid)

            assertEquals(1, retrieved.size)
            assertEquals(user.name, retrieved[0].name)
            assertEquals(user.emailAddress, retrieved[0].emailAddress)
            assertEquals(testOfficeUuid, retrieved[0].officeUuid)
            assertTrue(retrieved[0].isVegetarian)
        }

        @Test
        fun `insert user with null office succeeds`() {
            val user =
                aUser(
                    name = "No Office User",
                    emailAddress = uniqueEmail("nooffice"),
                    officeUuid = null,
                )

            val insertResult = usersService.insert(user)

            assertEquals(1, insertResult)
            val retrieved = usersService.getByUuid(user.uuid)[0]
            assertNull(retrieved.officeUuid)
        }

        @Test
        fun `insert user with all dietary restrictions`() {
            val user =
                aUser(
                    name = "Restricted User",
                    emailAddress = uniqueEmail("restricted"),
                    officeUuid = testOfficeUuid,
                    isVegetarian = true,
                    hasHalalRestriction = true,
                    hasNutsRestriction = true,
                    hasSeafoodRestriction = true,
                    hasPorkRestriction = true,
                    hasBeefRestriction = true,
                    isGlutenIntolerant = true,
                    isLactoseIntolerant = true,
                    otherRestrictions = "Many restrictions",
                )

            usersService.insert(user)
            val retrieved = usersService.getByUuid(user.uuid)[0]

            assertTrue(retrieved.isVegetarian, "isVegetarian should be true")
            assertTrue(retrieved.hasHalalRestriction, "hasHalalRestriction should be true")
            assertTrue(retrieved.hasNutsRestriction, "hasNutsRestriction should be true")
            assertTrue(retrieved.hasSeafoodRestriction, "hasSeafoodRestriction should be true")
            assertTrue(retrieved.hasPorkRestriction, "hasPorkRestriction should be true")
            assertTrue(retrieved.hasBeefRestriction, "hasBeefRestriction should be true")
            assertTrue(retrieved.isGlutenIntolerant, "isGlutenIntolerant should be true")
            assertTrue(retrieved.isLactoseIntolerant, "isLactoseIntolerant should be true")
            assertEquals("Many restrictions", retrieved.otherRestrictions)
        }

        @Test
        fun `insert user with default dietary restrictions as false`() {
            val user =
                aUser(
                    name = "Default User",
                    emailAddress = uniqueEmail("default"),
                    officeUuid = testOfficeUuid,
                )

            usersService.insert(user)
            val retrieved = usersService.getByUuid(user.uuid)[0]

            assertFalse(retrieved.isVegetarian)
            assertFalse(retrieved.hasHalalRestriction)
            assertFalse(retrieved.hasNutsRestriction)
            assertFalse(retrieved.hasSeafoodRestriction)
            assertFalse(retrieved.hasPorkRestriction)
            assertFalse(retrieved.hasBeefRestriction)
            assertFalse(retrieved.isGlutenIntolerant)
            assertFalse(retrieved.isLactoseIntolerant)
        }
    }

    @Nested
    inner class ReadOperations {
        @Test
        fun `getAll returns only non-deleted users`() {
            val user1 = aUser(name = "User 1", emailAddress = uniqueEmail("user1"), officeUuid = testOfficeUuid)
            val user2 = aUser(name = "User 2", emailAddress = uniqueEmail("user2"), officeUuid = testOfficeUuid)
            val deletedUser = aUser(name = "Deleted", emailAddress = uniqueEmail("deleted"), officeUuid = testOfficeUuid, isDeleted = true)

            usersService.insert(user1)
            usersService.insert(user2)
            usersService.insert(deletedUser)

            val allUsers = usersService.getAll()

            assertEquals(2, allUsers.size, "Should return only non-deleted users")
            assertTrue(allUsers.none { it.uuid == deletedUser.uuid }, "Deleted user should not be in results")
        }

        @Test
        fun `getAll returns empty list when no users exist`() {
            val allUsers = usersService.getAll()

            assertTrue(allUsers.isEmpty(), "Should return empty list when no users exist")
        }

        @Test
        fun `getByUuid returns user when it exists`() {
            val user = aUser(name = "Test User", emailAddress = uniqueEmail("test"), officeUuid = testOfficeUuid)
            usersService.insert(user)

            val retrieved = usersService.getByUuid(user.uuid)

            assertEquals(1, retrieved.size, "Should return exactly one user")
            assertEquals(user.uuid, retrieved[0].uuid)
        }

        @Test
        fun `getByUuid returns empty list for non-existent UUID`() {
            val nonExistentUuid = UUID.randomUUID()

            val result = usersService.getByUuid(nonExistentUuid)

            assertTrue(result.isEmpty(), "Should return empty list for non-existent UUID")
        }

        @Test
        fun `getByUuid returns deleted user without filtering`() {
            val deletedUser =
                aUser(name = "Deleted User", emailAddress = uniqueEmail("deleted"), officeUuid = testOfficeUuid, isDeleted = true)
            usersService.insert(deletedUser)

            val retrieved = usersService.getByUuid(deletedUser.uuid)

            assertEquals(1, retrieved.size, "getByUuid should return deleted users")
            assertTrue(retrieved[0].isDeleted)
        }

        @Test
        fun `getByEmailAddress returns user when email exists`() {
            val email = uniqueEmail("jane")
            val user = aUser(name = "Jane Doe", emailAddress = email, officeUuid = testOfficeUuid)
            usersService.insert(user)

            val retrieved = usersService.getByEmailAddress(email)

            assertEquals(user.uuid, retrieved?.uuid)
            assertEquals(user.name, retrieved?.name)
        }

        @Test
        fun `getByEmailAddress returns null for non-existent email`() {
            val retrieved = usersService.getByEmailAddress("nonexistent@lunatech.nl")

            assertNull(retrieved, "Should return null for non-existent email")
        }
    }

    @Nested
    inner class UpdateOperations {
        @Test
        fun `update returns 1 when user is successfully updated`() {
            val user = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
            usersService.insert(user)

            val updatedUser =
                UpdatedUser(
                    officeUuid = testOfficeUuid,
                    isVegetarian = true,
                    hasHalalRestriction = false,
                    hasNutsRestriction = false,
                    hasSeafoodRestriction = false,
                    hasPorkRestriction = false,
                    hasBeefRestriction = false,
                    isGlutenIntolerant = false,
                    isLactoseIntolerant = false,
                    otherRestrictions = "",
                )

            val updateResult = usersService.update(user.uuid, updatedUser)

            assertEquals(1, updateResult, "Update should return 1 for successful update")
        }

        @Test
        fun `update modifies user dietary restrictions`() {
            val user =
                aUser(
                    name = "John Doe",
                    emailAddress = uniqueEmail("john"),
                    officeUuid = testOfficeUuid,
                    isVegetarian = false,
                )
            usersService.insert(user)

            val updatedUser =
                UpdatedUser(
                    officeUuid = testOfficeUuid,
                    isVegetarian = true,
                    hasHalalRestriction = true,
                    hasNutsRestriction = true,
                    hasSeafoodRestriction = false,
                    hasPorkRestriction = false,
                    hasBeefRestriction = false,
                    isGlutenIntolerant = false,
                    isLactoseIntolerant = false,
                    otherRestrictions = "No spicy food",
                )
            usersService.update(user.uuid, updatedUser)

            val retrieved = usersService.getByUuid(user.uuid)[0]

            assertTrue(retrieved.isVegetarian)
            assertTrue(retrieved.hasHalalRestriction)
            assertTrue(retrieved.hasNutsRestriction)
            assertEquals("No spicy food", retrieved.otherRestrictions)
        }

        @Test
        fun `update returns 0 for non-existent user`() {
            val nonExistentUuid = UUID.randomUUID()
            val updatedUser =
                UpdatedUser(
                    officeUuid = testOfficeUuid,
                    isVegetarian = false,
                    hasHalalRestriction = false,
                    hasNutsRestriction = false,
                    hasSeafoodRestriction = false,
                    hasPorkRestriction = false,
                    hasBeefRestriction = false,
                    isGlutenIntolerant = false,
                    isLactoseIntolerant = false,
                    otherRestrictions = "",
                )

            val updateResult = usersService.update(nonExistentUuid, updatedUser)

            assertEquals(0, updateResult, "Update should return 0 for non-existent user")
        }
    }

    @Nested
    inner class DeleteOperations {
        @Test
        fun `delete performs soft delete by setting isDeleted flag`() {
            val user = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
            usersService.insert(user)

            usersService.delete(user.uuid)

            val retrieved = usersService.getByUuid(user.uuid)
            assertEquals(1, retrieved.size, "User should still exist in database")
            assertTrue(retrieved[0].isDeleted, "isDeleted flag should be set to true")
        }

        @Test
        fun `delete removes user from getAll results`() {
            val user = aUser(name = "John Doe", emailAddress = uniqueEmail("john"), officeUuid = testOfficeUuid)
            usersService.insert(user)

            usersService.delete(user.uuid)

            val allUsers = usersService.getAll()
            assertTrue(allUsers.isEmpty(), "Deleted user should not appear in getAll")
        }

        @Test
        fun `delete returns 0 for non-existent user`() {
            val nonExistentUuid = UUID.randomUUID()

            val deleteResult = usersService.delete(nonExistentUuid)

            assertEquals(0, deleteResult, "Delete should return 0 for non-existent user")
        }
    }
}
