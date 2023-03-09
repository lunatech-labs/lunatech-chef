package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.domain.ScheduleWithMenuInfo
import com.lunatech.chef.api.persistence.schemas.Locations
import com.lunatech.chef.api.persistence.schemas.Schedules
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate
import java.util.UUID

class SchedulesWithMenuInfo(
    val database: Database,
    private val menusWithDishesService: MenusWithDishesNamesService,
) {
    fun getAll(): List<ScheduleWithMenuInfo> =
        database.from(Schedules).select()
            .where { Schedules.isDeleted eq false }
            .orderBy(Schedules.date.asc())
            .map { Schedules.createEntity(it) }
            .map { getScheduleWithMenuInfo(it) }

    fun getFiltered(fromDate: LocalDate?, location: UUID?): List<ScheduleWithMenuInfo> =
        database.from(Schedules).select()
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()

                conditions += Schedules.isDeleted eq false

                if (fromDate != null) {
                    conditions += Schedules.date greaterEq fromDate
                }
                if (location != null) {
                    conditions += Schedules.locationUuid eq location
                }

                conditions.reduce { a, b -> a and b }
            }
            .orderBy(Schedules.date.asc())
            .map { Schedules.createEntity(it) }
            .map { getScheduleWithMenuInfo(it) }

    fun getByUuid(uuid: UUID): List<ScheduleWithMenuInfo> =
        database.from(Schedules).select()
            .where { Schedules.uuid eq uuid }
            .map { Schedules.createEntity(it) }
            .map { schedule ->
                val menu = menusWithDishesService.getByUuid(schedule.menuUuid)
                val location = database.from(Locations).select()
                    .where { Locations.uuid eq schedule.locationUuid }
                    .map { Locations.createEntity(it) }.firstOrNull()

                ScheduleWithMenuInfo(schedule.uuid, menu!!, schedule.date, location!!, schedule.isDeleted, schedule.date.toString())
            }

    private fun getScheduleWithMenuInfo(schedule: Schedule): ScheduleWithMenuInfo {
        val menu = menusWithDishesService.getByUuid(schedule.menuUuid)
        val location = database.from(Locations).select()
            .where { Locations.uuid eq schedule.locationUuid }
            .map { Locations.createEntity(it) }.firstOrNull()

        return ScheduleWithMenuInfo(schedule.uuid, menu!!, schedule.date, location!!, schedule.isDeleted, schedule.date.toString())
    }
}
