package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.domain.ScheduleWithInfo
import com.lunatech.chef.api.persistence.schemas.Locations
import com.lunatech.chef.api.persistence.schemas.Schedules
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.where

class SchedulesWithInfoService(val database: Database, private val menusWithDishes: MenusWithDishesNamesService) {
    fun getAll(): List<ScheduleWithInfo> =
        database.from(Schedules).select()
            .where { Schedules.isDeleted eq false }
            .map { Schedules.createEntity(it) }
            .map { getScheduleWithInfo(it) }

    fun getByUuid(uuid: UUID): List<ScheduleWithInfo> =
        database.from(Schedules).select()
            .where { (Schedules.uuid eq uuid) and (Schedules.isDeleted eq false) }
            .map { Schedules.createEntity(it) }
            .map { schedule ->
                val menu = menusWithDishes.getByUuid(schedule.menuUuid)
                val location = database.from(Locations).select()
                    .where { Locations.uuid eq schedule.locationUuid }
                    .map { Locations.createEntity(it) }.firstOrNull()

                ScheduleWithInfo(schedule.uuid, menu!!, schedule.date, location!!)
            }

    private fun getScheduleWithInfo(schedule: Schedule): ScheduleWithInfo {
        val menu = menusWithDishes.getByUuid(schedule.menuUuid)
        val location = database.from(Locations).select()
            .where { Locations.uuid eq schedule.locationUuid }
            .map { Locations.createEntity(it) }.firstOrNull()

        return ScheduleWithInfo(schedule.uuid, menu!!, schedule.date, location!!)
    }
}
