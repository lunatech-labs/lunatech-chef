package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Location
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Locations
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.routes.UpdatedLocation
import java.util.UUID
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where

class LocationsService(val database: Database) {
    fun getAll() = database.from(Locations).select().where { Locations.isDeleted eq false }.map { Locations.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Location> =
        database.from(Locations).select().where { Locations.uuid eq uuid }.map { Locations.createEntity(it) }

    fun insert(location: Location): Int =
        database.insert(Locations) {
            set(it.uuid, location.uuid)
            set(it.city, location.city)
            set(it.country, location.country)
            set(it.isDeleted, location.isDeleted)
        }

    fun update(uuid: UUID, location: UpdatedLocation): Int =
        database.update(Locations) {
            set(it.city, location.city)
            set(it.country, location.country)
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(Locations) {
        set(it.isDeleted, true)
        where {
            it.uuid eq uuid
        }

        // update related schedules and attendances
        val schedulesUuid = database
            .from(Schedules)
            .select()
            .where { Schedules.locationUuid eq uuid }
            .map { sch -> Schedules.createEntity(sch) }
            .map { schedule -> schedule.uuid }
        database.update(Schedules) { sch ->
        set(sch.isDeleted, true)
            where {
                sch.locationUuid eq uuid
            }
        }
        schedulesUuid.map { scheduleUuid ->
            database.update(Attendances) { attendance ->
                set(attendance.isDeleted, true)
                where {
                    attendance.scheduleUuid eq scheduleUuid
                }
            }
        }
    }
}
