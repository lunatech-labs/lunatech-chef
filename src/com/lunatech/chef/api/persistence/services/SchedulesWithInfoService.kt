package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.domain.ScheduleWithAttendanceInfo
import com.lunatech.chef.api.domain.ScheduleWithMenuInfo
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.DEFAULT_STRING
import com.lunatech.chef.api.persistence.schemas.Locations
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.persistence.schemas.Users
import java.time.LocalDate
import java.util.UUID
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.selectDistinct
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring

class SchedulesWithInfoService(
  val database: Database,
  private val menusWithDishesService: MenusWithDishesNamesService,
  private val menusService: MenusService
) {
    fun getAll(): List<ScheduleWithMenuInfo> =
        database.from(Schedules).select()
            .where { Schedules.isDeleted eq false }
            .map { Schedules.createEntity(it) }
            .map { getScheduleWithMenuInfo(it) }

    fun getFiltered(fromDate: LocalDate?, location: UUID?): List<ScheduleWithMenuInfo> =
        database.from(Schedules).select()
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()

                if (fromDate != null) {
                    conditions += Schedules.date greaterEq fromDate
                }
                if (location != null) {
                    conditions += Schedules.location eq location
                }

                conditions.reduce { a, b -> a and b }
            }
            .map { Schedules.createEntity(it) }
            .map { getScheduleWithMenuInfo(it) }

    fun getByUuid(uuid: UUID): List<ScheduleWithMenuInfo> =
        database.from(Schedules).select()
            .where { (Schedules.uuid eq uuid) and (Schedules.isDeleted eq false) }
            .map { Schedules.createEntity(it) }
            .map { schedule ->
                val menu = menusWithDishesService.getByUuid(schedule.menuUuid)
                val location = database.from(Locations).select()
                    .where { Locations.uuid eq schedule.locationUuid }
                    .map { Locations.createEntity(it) }.firstOrNull()

                ScheduleWithMenuInfo(schedule.uuid, menu!!, schedule.date, location!!)
            }

    private fun getScheduleWithMenuInfo(schedule: Schedule): ScheduleWithMenuInfo {
        val menu = menusWithDishesService.getByUuid(schedule.menuUuid)
        val location = database.from(Locations).select()
            .where { Locations.uuid eq schedule.locationUuid }
            .map { Locations.createEntity(it) }.firstOrNull()

        return ScheduleWithMenuInfo(schedule.uuid, menu!!, schedule.date, location!!)
    }

    fun getAllSchedulesWithAttendanceInfo(): List<ScheduleWithAttendanceInfo> =
        database.from(Schedules).select()
            .where { Schedules.isDeleted eq false }
            .map { Schedules.createEntity(it) }
            .map { getScheduleWithAttendanceInfo(it) }

    private fun getScheduleWithAttendanceInfo(schedule: Schedule): ScheduleWithAttendanceInfo {
        val menu = menusService.getByUuid(schedule.menuUuid)
        val location = database.from(Locations).select()
            .where { Locations.uuid eq schedule.locationUuid }
            .map { Locations.createEntity(it) }.firstOrNull()
        val attendants =
            database.from(Users)
                .leftJoin(Attendances, on = Attendances.userUuid eq Users.uuid)
                .selectDistinct(Users.name)
                .where { (Attendances.scheduleUuid eq schedule.uuid) and (Attendances.isAttending eq true) and (Attendances.isDeleted eq false) }
                .map { row -> row[Users.name] ?: DEFAULT_STRING }

        return ScheduleWithAttendanceInfo(schedule.uuid, menu!!.name, attendants, schedule.date, location!!)
    }
}
