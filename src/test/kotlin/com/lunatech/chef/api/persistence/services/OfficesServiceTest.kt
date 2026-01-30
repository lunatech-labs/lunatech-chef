package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.routes.UpdatedOffice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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

    @Nested
    inner class InsertOperations {
        @Test
        fun `insert returns 1 when office is successfully created`() {
            val office = anOffice(city = "Rotterdam", country = "Netherlands")

            val insertResult = officesService.insert(office)

            assertEquals(1, insertResult, "Insert should return 1 for successful creation")
        }

        @Test
        fun `insert persists all office properties correctly`() {
            val office = anOffice(city = "Rotterdam", country = "Netherlands")

            officesService.insert(office)
            val retrieved = officesService.getByUuid(office.uuid)

            assertEquals(1, retrieved.size)
            assertEquals(office.city, retrieved[0].city)
            assertEquals(office.country, retrieved[0].country)
        }
    }

    @Nested
    inner class ReadOperations {
        @Test
        fun `getAll returns only non-deleted offices`() {
            val office1 = anOffice(city = "Rotterdam")
            val office2 = anOffice(city = "Paris", country = "France")
            val deletedOffice = anOffice(city = "Berlin", country = "Germany", isDeleted = true)

            officesService.insert(office1)
            officesService.insert(office2)
            officesService.insert(deletedOffice)

            val allOffices = officesService.getAll()

            assertEquals(2, allOffices.size, "Should return only non-deleted offices")
            assertTrue(allOffices.none { it.uuid == deletedOffice.uuid }, "Deleted office should not be in results")
        }

        @Test
        fun `getAll returns empty list when no offices exist`() {
            val allOffices = officesService.getAll()

            assertTrue(allOffices.isEmpty(), "Should return empty list when no offices exist")
        }

        @Test
        fun `getByUuid returns office when it exists`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)

            val retrieved = officesService.getByUuid(office.uuid)

            assertEquals(1, retrieved.size, "Should return exactly one office")
            assertEquals(office.uuid, retrieved[0].uuid)
        }

        @Test
        fun `getByUuid returns empty list for non-existent UUID`() {
            val nonExistentUuid = UUID.randomUUID()

            val result = officesService.getByUuid(nonExistentUuid)

            assertTrue(result.isEmpty(), "Should return empty list for non-existent UUID")
        }

        @Test
        fun `getByUuid returns deleted office without filtering`() {
            val deletedOffice = anOffice(city = "Deleted City", isDeleted = true)
            officesService.insert(deletedOffice)

            val retrieved = officesService.getByUuid(deletedOffice.uuid)

            assertEquals(1, retrieved.size, "getByUuid should return deleted offices")
            assertTrue(retrieved[0].isDeleted)
        }
    }

    @Nested
    inner class UpdateOperations {
        @Test
        fun `update returns 1 when office is successfully updated`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)

            val updatedOffice = UpdatedOffice(city = "Amsterdam", country = "Netherlands")

            val updateResult = officesService.update(office.uuid, updatedOffice)

            assertEquals(1, updateResult, "Update should return 1 for successful update")
        }

        @Test
        fun `update modifies office properties`() {
            val office = anOffice(city = "Rotterdam", country = "Netherlands")
            officesService.insert(office)

            val updatedOffice = UpdatedOffice(city = "Amsterdam", country = "Netherlands")
            officesService.update(office.uuid, updatedOffice)

            val retrieved = officesService.getByUuid(office.uuid)[0]

            assertEquals("Amsterdam", retrieved.city)
            assertEquals("Netherlands", retrieved.country)
        }

        @Test
        fun `update returns 0 for non-existent office`() {
            val nonExistentUuid = UUID.randomUUID()
            val updatedOffice = UpdatedOffice(city = "Amsterdam", country = "Netherlands")

            val updateResult = officesService.update(nonExistentUuid, updatedOffice)

            assertEquals(0, updateResult, "Update should return 0 for non-existent office")
        }
    }

    @Nested
    inner class DeleteOperations {
        @Test
        fun `delete performs soft delete by setting isDeleted flag`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)

            officesService.delete(office.uuid)

            val retrieved = officesService.getByUuid(office.uuid)
            assertEquals(1, retrieved.size, "Office should still exist in database")
            assertTrue(retrieved[0].isDeleted, "isDeleted flag should be set to true")
        }

        @Test
        fun `delete removes office from getAll results`() {
            val office = anOffice(city = "Rotterdam")
            officesService.insert(office)

            officesService.delete(office.uuid)

            val allOffices = officesService.getAll()
            assertTrue(allOffices.isEmpty(), "Deleted office should not appear in getAll")
        }

        @Test
        fun `delete returns 0 for non-existent office`() {
            val nonExistentUuid = UUID.randomUUID()

            val deleteResult = officesService.delete(nonExistentUuid)

            assertEquals(0, deleteResult, "Delete should return 0 for non-existent office")
        }
    }
}
