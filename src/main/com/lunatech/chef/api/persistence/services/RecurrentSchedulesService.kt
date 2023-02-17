package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.RecurrentSchedule
import com.lunatech.chef.api.persistence.schemas.RecurrentSchedules
import com.lunatech.chef.api.routes.UpdatedRecurrentSchedule
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.greater
import org.ktorm.dsl.insert
import org.ktorm.dsl.lessEq
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import java.time.LocalDate
import java.util.UUID

class RecurrentSchedulesService(val database: Database) {
    fun getAll() = database.from(RecurrentSchedules).select()
        .where { RecurrentSchedules.isDeleted eq false }
        .map { RecurrentSchedules.createEntity(it) }

    fun getIntervalDate(fromDate: LocalDate, toDate: LocalDate): List<RecurrentSchedule> =
        database.from(RecurrentSchedules).select()
            .where { (RecurrentSchedules.isDeleted eq false) }
            .where { (RecurrentSchedules.nextDate greater fromDate) and (RecurrentSchedules.nextDate lessEq toDate) }
            .map { RecurrentSchedules.createEntity(it) }

    fun getByUuid(uuid: UUID): List<RecurrentSchedule> =
        database.from(RecurrentSchedules).select()
            .where { RecurrentSchedules.uuid eq uuid }
            .map { RecurrentSchedules.createEntity(it) }

    fun insert(recurrentSchedule: RecurrentSchedule): Int =
        database.insert(RecurrentSchedules) {
            set(it.uuid, recurrentSchedule.uuid)
            set(it.menuUuid, recurrentSchedule.menuUuid)
            set(it.locationUuid, recurrentSchedule.locationUuid)
            set(it.repetitionDays, recurrentSchedule.repetitionDays)
            set(it.nextDate, recurrentSchedule.nextDate)
            set(it.isDeleted, recurrentSchedule.isDeleted)
        }

    fun update(uuid: UUID, recurrentSchedule: UpdatedRecurrentSchedule): Int =
        database.update(RecurrentSchedules) {
            set(it.menuUuid, recurrentSchedule.menuUuid)
            set(it.locationUuid, recurrentSchedule.locationUuid)
            set(it.repetitionDays, recurrentSchedule.repetitionDays)
            set(it.nextDate, recurrentSchedule.nextDate)
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int {
        val result = database.update(RecurrentSchedules) {
            set(it.isDeleted, true)
            where {
                it.uuid eq uuid
            }
        }

        return result
    }
}
