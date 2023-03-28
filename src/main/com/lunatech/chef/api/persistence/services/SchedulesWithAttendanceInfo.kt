package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.domain.ScheduleWithAttendanceInfo
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Locations
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.persistence.schemas.Users
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.desc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.selectDistinct
import org.ktorm.dsl.where
import org.ktorm.schema.ColumnDeclaring
import java.sql.PreparedStatement
import java.time.LocalDate
import java.util.UUID
import kotlin.math.log

class SchedulesWithAttendanceInfo(
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

        // use raw SQL to get the distinct users
        val attendants = database.useConnection {connection ->
        val scheduleId = schedule.uuid
            val statement = """
         SELECT *
         FROM (SELECT DISTINCT ON (u.uuid) u.uuid, a.created_at, is_attending isA, *
              FROM users u
                       LEFT JOIN attendances a on u.uuid = a.user_uuid
              WHERE a.schedule_uuid = '$scheduleId'
                AND a.is_deleted = false
              ORDER BY u.uuid, a.created_at DESC) tu
        WHERE tu.is_attending = true;
            """.trimIndent()
            connection.prepareStatement(statement).use {
                extractRawQueryToUser(it)
            }

        }
        return ScheduleWithAttendanceInfo(schedule.uuid, menu!!.name, attendants, schedule.date, location!!)
    }

    private fun extractRawQueryToUser(it: PreparedStatement): List<User> {
       return it.executeQuery().use { resultSet ->
            val users = mutableListOf<User>()
            while (resultSet.next()) {
                users.add(
                    User(
                        UUID.fromString(resultSet.getString("uuid")),
                        resultSet.getString("name"),
                        resultSet.getString("email_address"),
                        UUID.fromString(resultSet.getString("location_uuid")),
                        resultSet.getBoolean("is_vegetarian"),
                        resultSet.getBoolean("has_halal_restriction"),
                        resultSet.getBoolean("has_nuts_restriction"),
                        resultSet.getBoolean("has_seafood_restriction"),
                        resultSet.getBoolean("has_pork_restriction"),
                        resultSet.getBoolean("has_beef_restriction"),
                        resultSet.getBoolean("is_gluten_intolerant"),
                        resultSet.getBoolean("is_lactose_intolerant"),
                        resultSet.getString("other_restrictions"),
                        resultSet.getBoolean("is_inactive"),
                        resultSet.getBoolean("is_deleted")
                    )
                )
            }
            users
        }
    }
}
