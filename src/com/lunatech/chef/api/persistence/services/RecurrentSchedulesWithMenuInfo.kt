package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.RecurrentSchedule
import com.lunatech.chef.api.domain.RecurrentScheduleWithMenuInfo
import com.lunatech.chef.api.persistence.schemas.Locations
import com.lunatech.chef.api.persistence.schemas.RecurrentSchedules
import java.util.UUID
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring

class RecurrentSchedulesWithMenuInfo(
  val database: Database,
  private val menusWithDishesService: MenusWithDishesNamesService
) {
    fun getAll(): List<RecurrentScheduleWithMenuInfo> =
        database.from(RecurrentSchedules).select()
            .where { RecurrentSchedules.isDeleted eq false }
            .map { RecurrentSchedules.createEntity(it) }
            .map { getScheduleWithMenuInfo(it) }

    fun getFiltered(location: UUID?): List<RecurrentScheduleWithMenuInfo> =
        database.from(RecurrentSchedules).select()
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()

                conditions += RecurrentSchedules.isDeleted eq false

                if (location != null) {
                    conditions += RecurrentSchedules.locationUuid eq location
                }

                conditions.reduce { a, b -> a and b }
            }
            .map { RecurrentSchedules.createEntity(it) }
            .map { getScheduleWithMenuInfo(it) }

    fun getByUuid(uuid: UUID): List<RecurrentScheduleWithMenuInfo> =
        database.from(RecurrentSchedules).select()
            .where { (RecurrentSchedules.uuid eq uuid) and (RecurrentSchedules.isDeleted eq false) }
            .map { RecurrentSchedules.createEntity(it) }
            .map { schedule ->
                val menu = menusWithDishesService.getByUuid(schedule.menuUuid)
                val location = database.from(Locations).select()
                    .where { Locations.uuid eq schedule.locationUuid }
                    .map { Locations.createEntity(it) }.firstOrNull()

                RecurrentScheduleWithMenuInfo(schedule.uuid, menu!!, schedule.nextDate, location!!)
            }

    private fun getScheduleWithMenuInfo(recurrentSchedule: RecurrentSchedule): RecurrentScheduleWithMenuInfo {
        val menu = menusWithDishesService.getByUuid(recurrentSchedule.menuUuid)
        val location = database.from(Locations).select()
            .where { Locations.uuid eq recurrentSchedule.locationUuid }
            .map { Locations.createEntity(it) }.firstOrNull()

        return RecurrentScheduleWithMenuInfo(recurrentSchedule.uuid, menu!!, recurrentSchedule.nextDate, location!!)
    }
}
