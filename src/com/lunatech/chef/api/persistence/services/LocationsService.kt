package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Location
import com.lunatech.chef.api.persistence.schemas.Locations
import com.lunatech.chef.api.routes.UpdatedLocation
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.dsl.where

class LocationsService(val database: Database) {
    fun getAll() = database.from(Locations).select().where { Locations.isDeleted eq false }.map { Locations.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Location> =
        database.from(Locations).select().where { Locations.uuid eq uuid }.map { Locations.createEntity(it) }

    fun insert(location: Location): Int =
        database.insert(Locations) {
            it.uuid to location.uuid
            it.city to location.city
            it.country to location.country
            it.isDeleted to location.isDeleted
        }

    fun update(uuid: UUID, location: UpdatedLocation): Int =
        database.update(Locations) {
            it.city to location.city
            it.country to location.country
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(Locations) {
        it.isDeleted to true
        where {
            it.uuid eq uuid
        }
    }
}
