package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.routes.UpdatedSchedule
import org.ktorm.database.Database
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.gte
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import java.time.LocalDate
import java.util.UUID

class SchedulesService(
    private val database: Database,
) {
    fun getAll(): List<Schedule> =
        database
            .from(Schedules)
            .select()
            .where { Schedules.isDeleted eq false }
            .orderBy(Schedules.date.asc())
            .map { Schedules.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Schedule> =
        database
            .from(Schedules)
            .select()
            .where { Schedules.uuid eq uuid }
            .map { Schedules.createEntity(it) }

    fun getAfterDate(date: LocalDate): List<Schedule> =
        database
            .from(Schedules)
            .select()
            .where { Schedules.date gte date }
            .map { Schedules.createEntity(it) }

    fun insert(schedule: Schedule): Int =
        database.insert(Schedules) {
            set(it.uuid, schedule.uuid)
            set(it.menuUuid, schedule.menuUuid)
            set(it.date, schedule.date)
            set(it.officeUuid, schedule.officeUuid)
            set(it.isDeleted, schedule.isDeleted)
        }

    fun update(
        uuid: UUID,
        schedule: UpdatedSchedule,
    ): Int =
        database.update(Schedules) {
            set(it.menuUuid, schedule.menuUuid)
            set(it.date, schedule.date)
            set(it.officeUuid, schedule.officeUuid)
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int {
        val result =
            database.update(Schedules) {
                set(it.isDeleted, true)
                where {
                    it.uuid eq uuid
                }
            }
        // delete related attendances
        database.update(Attendances) {
            set(it.isDeleted, true)
            where {
                it.scheduleUuid eq uuid
            }
        }
        return result
    }
}
