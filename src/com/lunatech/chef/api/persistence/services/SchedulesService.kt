package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.routes.UpdatedSchedule
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.dsl.where

class SchedulesService(val database: Database) {
    fun getAll() = database.from(Schedules).select().where { Schedules.isDeleted eq false }.map { Schedules.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Schedule> =
        database.from(Schedules).select().where { -> Schedules.uuid eq uuid }.map { Schedules.createEntity(it) }

    fun insert(schedule: Schedule): Int =
        database.insert(Schedules) {
            it.uuid to schedule.uuid
            it.menuUuid to schedule.menuUuid
            it.date to schedule.date
            it.location to schedule.locationUuid
            it.isDeleted to schedule.isDeleted
        }

    fun update(uuid: UUID, schedule: UpdatedSchedule): Int =
        database.update(Schedules) {
            it.menuUuid to schedule.menuUuid
            it.date to schedule.date
            it.location to schedule.locationUuid
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(Schedules) {
        it.isDeleted to true
        where {
            it.uuid eq uuid
        }
    }
}
