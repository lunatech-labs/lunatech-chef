package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.domain.ScheduleWithMenuInfo
import com.lunatech.chef.api.persistence.schemas.Offices
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

class SchedulesWithMenuInfoService(
    val database: Database,
    private val menusWithDishesService: MenusWithDishesNamesService,
) {
    fun getAll(): List<ScheduleWithMenuInfo> =
        database.from(Schedules).select()
            .where { Schedules.isDeleted eq false }
            .orderBy(Schedules.date.asc())
            .map { Schedules.createEntity(it) }
            .map { getScheduleWithMenuInfo(it) }

    fun getFiltered(
        fromDate: LocalDate?,
        office: UUID?,
    ): List<ScheduleWithMenuInfo> =
        database.from(Schedules).select()
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()

                conditions += Schedules.isDeleted eq false

                if (fromDate != null) {
                    conditions += Schedules.date greaterEq fromDate
                }
                if (office != null) {
                    conditions += Schedules.officeUuid eq office
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
                val office =
                    database.from(Offices).select()
                        .where { Offices.uuid eq schedule.officeUuid }
                        .map { Offices.createEntity(it) }.firstOrNull()

                ScheduleWithMenuInfo(
                    schedule.uuid,
                    menu!!,
                    schedule.date,
                    office!!,
                )
            }

    private fun getScheduleWithMenuInfo(schedule: Schedule): ScheduleWithMenuInfo {
        val menu = menusWithDishesService.getByUuid(schedule.menuUuid)
        val office =
            database.from(Offices).select()
                .where { Offices.uuid eq schedule.officeUuid }
                .map { Offices.createEntity(it) }.firstOrNull()

        return ScheduleWithMenuInfo(
            schedule.uuid,
            menu!!,
            schedule.date,
            office!!,
        )
    }
}
