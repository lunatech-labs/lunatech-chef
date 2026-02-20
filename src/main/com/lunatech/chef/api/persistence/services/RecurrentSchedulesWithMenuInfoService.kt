package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.RecurrentSchedule
import com.lunatech.chef.api.domain.RecurrentScheduleWithMenuInfo
import com.lunatech.chef.api.persistence.schemas.Offices
import com.lunatech.chef.api.persistence.schemas.RecurrentSchedules
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring
import java.util.UUID

class RecurrentSchedulesWithMenuInfoService(
    val database: Database,
    private val menusWithDishesService: MenusWithDishesNamesService,
) {
    fun getAll(): List<RecurrentScheduleWithMenuInfo> =
        database
            .from(RecurrentSchedules)
            .select()
            .where { RecurrentSchedules.isDeleted eq false }
            .map { RecurrentSchedules.createEntity(it) }
            .map { getScheduleWithMenuInfo(it) }

    fun getFiltered(office: UUID?): List<RecurrentScheduleWithMenuInfo> =
        database
            .from(RecurrentSchedules)
            .select()
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()

                conditions += RecurrentSchedules.isDeleted eq false

                if (office != null) {
                    conditions += RecurrentSchedules.officeUuid eq office
                }

                conditions.reduce { a, b -> a and b }
            }.map { RecurrentSchedules.createEntity(it) }
            .map { getScheduleWithMenuInfo(it) }

    fun getByUuid(uuid: UUID): List<RecurrentScheduleWithMenuInfo> =
        database
            .from(RecurrentSchedules)
            .select()
            .where { RecurrentSchedules.uuid eq uuid }
            .map { RecurrentSchedules.createEntity(it) }
            .map { recSchedule ->
                val menu = menusWithDishesService.getByUuid(recSchedule.menuUuid)
                val office =
                    database
                        .from(Offices)
                        .select()
                        .where { Offices.uuid eq recSchedule.officeUuid }
                        .map { Offices.createEntity(it) }
                        .firstOrNull()

                RecurrentScheduleWithMenuInfo(
                    recSchedule.uuid,
                    menu!!,
                    recSchedule.nextDate,
                    office!!,
                    recSchedule.repetitionDays,
                )
            }

    private fun getScheduleWithMenuInfo(recSchedule: RecurrentSchedule): RecurrentScheduleWithMenuInfo {
        val menu = menusWithDishesService.getByUuid(recSchedule.menuUuid)
        val office =
            database
                .from(Offices)
                .select()
                .where { Offices.uuid eq recSchedule.officeUuid }
                .map { Offices.createEntity(it) }
                .firstOrNull()

        return RecurrentScheduleWithMenuInfo(
            recSchedule.uuid,
            menu!!,
            recSchedule.nextDate,
            office!!,
            recSchedule.repetitionDays,
        )
    }
}
