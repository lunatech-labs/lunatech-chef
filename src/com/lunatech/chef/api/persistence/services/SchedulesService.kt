package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.routes.UpdatedSchedule
import java.util.UUID
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where

class SchedulesService(val database: Database) {
    fun getAll() = database.from(Schedules).select().where { Schedules.isDeleted eq false }.map { Schedules.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Schedule> =
        database.from(Schedules).select().where { Schedules.uuid eq uuid }.map { Schedules.createEntity(it) }

    fun insert(schedule: Schedule): Int =
        database.insert(Schedules) {
            set(it.uuid, schedule.uuid)
            set(it.menuUuid, schedule.menuUuid)
            set(it.date, schedule.date)
            set(it.location, schedule.locationUuid)
            set(it.isDeleted, schedule.isDeleted)
        }

    fun update(uuid: UUID, schedule: UpdatedSchedule): Int =
        database.update(Schedules) {
            set(it.menuUuid, schedule.menuUuid)
            set(it.date, schedule.date)
            set(it.location, schedule.locationUuid)
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int {
        val result = database.update(Schedules) {
            set(it.isDeleted, true)
            where {
                it.uuid eq uuid
            }
        }
        database.update(Attendances) {
            set(it.isDeleted, true)
            where {
                it.scheduleUuid eq uuid
            }
        }
        return result
    }
}
