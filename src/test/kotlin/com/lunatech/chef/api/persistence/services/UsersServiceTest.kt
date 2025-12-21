package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Office
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.routes.UpdatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class UsersServiceTest {
    private lateinit var usersService: UsersService
    private lateinit var officesService: OfficesService
    private lateinit var testOffice: Office

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        usersService = UsersService(database)
        officesService = OfficesService(database)

        // Create a test office for user foreign key
        testOffice = Office(UUID.randomUUID(), "Rotterdam", "Netherlands", false)
        officesService.insert(testOffice)
    }

    @Test
    fun `insert and retrieve user`() {
        val user = User(
            uuid = UUID.randomUUID(),
            name = "John Doe",
            emailAddress = "john.doe@lunatech.nl",
            officeUuid = testOffice.uuid,
            isVegetarian = true,
            isDeleted = false
        )

        val insertResult = usersService.insert(user)
        assertEquals(1, insertResult)

        val retrieved = usersService.getByUuid(user.uuid)
        assertEquals(1, retrieved.size)
        assertEquals(user.name, retrieved[0].name)
        assertEquals(user.emailAddress, retrieved[0].emailAddress)
        assertEquals(testOffice.uuid, retrieved[0].officeUuid)
        assertTrue(retrieved[0].isVegetarian)
    }

    @Test
    fun `getByEmailAddress returns user`() {
        val user = User(
            uuid = UUID.randomUUID(),
            name = "Jane Doe",
            emailAddress = "jane.doe@lunatech.nl",
            officeUuid = testOffice.uuid,
            isDeleted = false
        )
        usersService.insert(user)

        val retrieved = usersService.getByEmailAddress("jane.doe@lunatech.nl")
        assertEquals(user.uuid, retrieved?.uuid)
        assertEquals(user.name, retrieved?.name)
    }

    @Test
    fun `getByEmailAddress returns null for non-existent email`() {
        val retrieved = usersService.getByEmailAddress("nonexistent@lunatech.nl")
        assertNull(retrieved)
    }

    @Test
    fun `getAll returns only non-deleted users`() {
        val user1 = User(UUID.randomUUID(), "User 1", "user1@lunatech.nl", testOffice.uuid, isDeleted = false)
        val user2 = User(UUID.randomUUID(), "User 2", "user2@lunatech.nl", testOffice.uuid, isDeleted = false)
        val deletedUser = User(UUID.randomUUID(), "Deleted", "deleted@lunatech.nl", testOffice.uuid, isDeleted = true)

        usersService.insert(user1)
        usersService.insert(user2)
        usersService.insert(deletedUser)

        val allUsers = usersService.getAll()
        assertEquals(2, allUsers.size)
        assertTrue(allUsers.none { it.uuid == deletedUser.uuid })
    }

    @Test
    fun `update user dietary restrictions`() {
        val user = User(
            uuid = UUID.randomUUID(),
            name = "John Doe",
            emailAddress = "john@lunatech.nl",
            officeUuid = testOffice.uuid,
            isVegetarian = false,
            isDeleted = false
        )
        usersService.insert(user)

        val updatedUser = UpdatedUser(
            officeUuid = testOffice.uuid,
            isVegetarian = true,
            hasHalalRestriction = true,
            hasNutsRestriction = true,
            hasSeafoodRestriction = false,
            hasPorkRestriction = false,
            hasBeefRestriction = false,
            isGlutenIntolerant = false,
            isLactoseIntolerant = false,
            otherRestrictions = "No spicy food"
        )
        val updateResult = usersService.update(user.uuid, updatedUser)
        assertEquals(1, updateResult)

        val retrieved = usersService.getByUuid(user.uuid)[0]
        assertTrue(retrieved.isVegetarian)
        assertTrue(retrieved.hasHalalRestriction)
        assertTrue(retrieved.hasNutsRestriction)
        assertEquals("No spicy food", retrieved.otherRestrictions)
    }

    @Test
    fun `delete user soft deletes`() {
        val user = User(
            uuid = UUID.randomUUID(),
            name = "John Doe",
            emailAddress = "john@lunatech.nl",
            officeUuid = testOffice.uuid,
            isDeleted = false
        )
        usersService.insert(user)

        usersService.delete(user.uuid)

        val allUsers = usersService.getAll()
        assertTrue(allUsers.isEmpty())

        val retrieved = usersService.getByUuid(user.uuid)
        assertEquals(1, retrieved.size)
        assertTrue(retrieved[0].isDeleted)
    }

    @Test
    fun `insert user with null office`() {
        val user = User(
            uuid = UUID.randomUUID(),
            name = "No Office User",
            emailAddress = "nooffice@lunatech.nl",
            officeUuid = null,
            isDeleted = false
        )

        val insertResult = usersService.insert(user)
        assertEquals(1, insertResult)

        val retrieved = usersService.getByUuid(user.uuid)[0]
        assertNull(retrieved.officeUuid)
    }

    @Test
    fun `insert user with all dietary restrictions`() {
        val user = User(
            uuid = UUID.randomUUID(),
            name = "Restricted User",
            emailAddress = "restricted@lunatech.nl",
            officeUuid = testOffice.uuid,
            isVegetarian = true,
            hasHalalRestriction = true,
            hasNutsRestriction = true,
            hasSeafoodRestriction = true,
            hasPorkRestriction = true,
            hasBeefRestriction = true,
            isGlutenIntolerant = true,
            isLactoseIntolerant = true,
            otherRestrictions = "Many restrictions",
            isInactive = false,
            isDeleted = false
        )

        usersService.insert(user)

        val retrieved = usersService.getByUuid(user.uuid)[0]
        assertTrue(retrieved.isVegetarian)
        assertTrue(retrieved.hasHalalRestriction)
        assertTrue(retrieved.hasNutsRestriction)
        assertTrue(retrieved.hasSeafoodRestriction)
        assertTrue(retrieved.hasPorkRestriction)
        assertTrue(retrieved.hasBeefRestriction)
        assertTrue(retrieved.isGlutenIntolerant)
        assertTrue(retrieved.isLactoseIntolerant)
        assertEquals("Many restrictions", retrieved.otherRestrictions)
    }
}
