package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.AttendanceForSlackbot
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.DEFAULT_STRING
import com.lunatech.chef.api.persistence.schemas.DEFAULT_UUID
import com.lunatech.chef.api.persistence.schemas.Locations
import com.lunatech.chef.api.persistence.schemas.MenuNames
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.persistence.schemas.Users
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.isNull
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.lessEq
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate

class AttendancesForSlackbotService(val database: Database) {
    fun getMissingAttendances(fromDate: LocalDate, untilDate: LocalDate): List<AttendanceForSlackbot> {
        return database.from(Attendances)
            .leftJoin(Schedules, on = Schedules.uuid eq Attendances.scheduleUuid)
            .leftJoin(MenuNames, on = Schedules.menuUuid eq MenuNames.uuid)
            .leftJoin(Users, on = Attendances.userUuid eq Users.uuid)
            .leftJoin(Locations, on = Schedules.locationUuid eq Locations.uuid)
            .select(Attendances.uuid, Users.emailAddress, Schedules.date, Locations.city, MenuNames.name)
            .where {
                val conditions = ArrayList<ColumnDeclaring<Boolean>>()

                conditions += Schedules.date greaterEq fromDate
                conditions += Schedules.date lessEq untilDate

                conditions += Attendances.isAttending.isNull()

                conditions += Schedules.isDeleted eq false
                conditions += Users.isInactive eq false
                conditions += Users.isDeleted eq false

                conditions.reduce { a, b -> a and b }
            }
            .orderBy(Schedules.date.asc())
            .map { row ->
                AttendanceForSlackbot(
                    row[Attendances.uuid] ?: DEFAULT_UUID,
                    row[Users.emailAddress] ?: DEFAULT_STRING,
                    row[Schedules.date] ?: LocalDate.now(),
                    row[Locations.city] ?: DEFAULT_STRING,
                    row[MenuNames.name] ?: DEFAULT_STRING,
                )
            }
    }
}
