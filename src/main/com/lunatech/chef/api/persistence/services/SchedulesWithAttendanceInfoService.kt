package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.domain.ScheduleWithAttendanceInfo
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Locations
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.persistence.schemas.Users
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate
import java.util.UUID

class SchedulesWithAttendanceInfoService(
    val database: Database,
    private val menusService: MenusService,
) {
    fun getFiltered(fromDate: LocalDate?, location: UUID?): List<ScheduleWithAttendanceInfo> =
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
            .map { getScheduleWithAttendanceInfo(it) }

    private fun getScheduleWithAttendanceInfo(schedule: Schedule): ScheduleWithAttendanceInfo {
        val menu = menusService.getByUuid(schedule.menuUuid)
        val location = database.from(Locations).select()
            .where { Locations.uuid eq schedule.locationUuid }
            .map { Locations.createEntity(it) }.firstOrNull()
        val attendants =
            database.from(Users)
                .leftJoin(Attendances, on = Attendances.userUuid eq Users.uuid)
                .select(
                    Users.uuid, Users.name, Users.emailAddress, Users.isVegetarian, Users.hasHalalRestriction,
                    Users.hasNutsRestriction, Users.hasSeafoodRestriction, Users.hasPorkRestriction,
                    Users.hasBeefRestriction, Users.isGlutenIntolerant, Users.isLactoseIntolerant,
                    Users.otherRestrictions, Users.isInactive, Users.isDeleted,
                )
                .where { (Attendances.scheduleUuid eq schedule.uuid) and (Attendances.isAttending eq true) and (Attendances.isDeleted eq false) }
                .map { Users.createEntity(it) }

        return ScheduleWithAttendanceInfo(schedule.uuid, menu!!.name, attendants, schedule.date, location!!)
    }
}
