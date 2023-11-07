package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Office
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Offices
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.routes.UpdatedOffice
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.greater
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate
import java.util.UUID

class OfficesService(val database: Database) {
    fun getAll() =
        database.from(Offices).select().where { Offices.isDeleted eq false }.map { Offices.createEntity(it) }

    fun getByUuid(uuid: UUID): List<Office> =
        database.from(Offices).select().where { Offices.uuid eq uuid }.map { Offices.createEntity(it) }

    fun insert(office: Office): Int =
        database.insert(Offices) {
            set(it.uuid, office.uuid)
            set(it.city, office.city)
            set(it.country, office.country)
            set(it.isDeleted, office.isDeleted)
        }

    fun update(uuid: UUID, office: UpdatedOffice): Int =
        database.update(Offices) {
            set(it.city, office.city)
            set(it.country, office.country)
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(Offices) {
        set(it.isDeleted, true)
        where {
            it.uuid eq uuid
        }

        // delete related schedules and attendances (after current date)
        val baseDate = LocalDate.now()
        val schedulesUuid = database
            .from(Schedules)
            .select()
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()
                conditions += Schedules.officeUuid eq uuid
                conditions += Schedules.date greater baseDate
                conditions.reduce { a, b -> a and b }
            }
            .map { sch -> Schedules.createEntity(sch) }
            .map { schedule -> schedule.uuid }

        database.update(Schedules) {
            set(it.isDeleted, true)
            where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()
                conditions += Schedules.officeUuid eq uuid
                conditions += Schedules.date greater baseDate
                conditions.reduce { a, b -> a and b }
            }
        }
        schedulesUuid.map { scheduleUuid ->
            database.update(Attendances) { attendance ->
                set(attendance.isDeleted, true)
                where {
                    attendance.scheduleUuid eq scheduleUuid
                }
            }
        }
    }
}
