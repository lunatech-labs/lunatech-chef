package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Office
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.routes.UpdatedOffice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class OfficesServiceTest {
    private lateinit var officesService: OfficesService

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        officesService = OfficesService(database)
    }

    @Test
    fun `insert and retrieve office`() {
        val office = Office(
            uuid = UUID.randomUUID(),
            city = "Rotterdam",
            country = "Netherlands",
            isDeleted = false
        )

        val insertResult = officesService.insert(office)
        assertEquals(1, insertResult)

        val retrieved = officesService.getByUuid(office.uuid)
        assertEquals(1, retrieved.size)
        assertEquals(office.city, retrieved[0].city)
        assertEquals(office.country, retrieved[0].country)
    }

    @Test
    fun `getAll returns only non-deleted offices`() {
        val office1 = Office(UUID.randomUUID(), "Rotterdam", "Netherlands", false)
        val office2 = Office(UUID.randomUUID(), "Paris", "France", false)
        val deletedOffice = Office(UUID.randomUUID(), "Berlin", "Germany", true)

        officesService.insert(office1)
        officesService.insert(office2)
        officesService.insert(deletedOffice)

        val allOffices = officesService.getAll()
        assertEquals(2, allOffices.size)
        assertTrue(allOffices.none { it.uuid == deletedOffice.uuid })
    }

    @Test
    fun `update office`() {
        val office = Office(UUID.randomUUID(), "Rotterdam", "Netherlands", false)
        officesService.insert(office)

        val updatedOffice = UpdatedOffice(city = "Amsterdam", country = "Netherlands")
        val updateResult = officesService.update(office.uuid, updatedOffice)
        assertEquals(1, updateResult)

        val retrieved = officesService.getByUuid(office.uuid)
        assertEquals("Amsterdam", retrieved[0].city)
    }

    @Test
    fun `delete office soft deletes`() {
        val office = Office(UUID.randomUUID(), "Rotterdam", "Netherlands", false)
        officesService.insert(office)

        officesService.delete(office.uuid)

        val allOffices = officesService.getAll()
        assertTrue(allOffices.isEmpty())

        // But getByUuid still finds it (no isDeleted filter)
        val retrieved = officesService.getByUuid(office.uuid)
        assertEquals(1, retrieved.size)
        assertTrue(retrieved[0].isDeleted)
    }

    @Test
    fun `getAll returns empty list when no offices`() {
        val allOffices = officesService.getAll()
        assertTrue(allOffices.isEmpty())
    }
}
