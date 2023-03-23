package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Users
import com.lunatech.chef.api.routes.UpdatedUser
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import java.util.UUID

class UsersService(val database: Database) {
    fun getAll(): List<User> = database.from(Users).select().where { Users.isDeleted eq false }
        .map { Users.createEntity(it) }

    fun getByUuid(uuid: UUID): List<User> =
        database.from(Users).select().where { Users.uuid eq uuid }.map { Users.createEntity(it) }

    fun getByEmail(email: String): List<User> =
        database.from(Users).select().where { Users.emailAddress eq email }.map { Users.createEntity(it) }

    fun getByEmailAddress(emailAddress: String): User? =
        database.from(Users).select()
            .where { -> Users.emailAddress eq emailAddress }
            .map { Users.createEntity(it) }
            .firstOrNull()

    fun insert(user: User): Int =
        database.insert(Users) {
            set(it.uuid, user.uuid)
            set(it.name, user.name)
            set(it.emailAddress, user.emailAddress)
            set(it.locationUuid, user.locationUuid)
            set(it.isVegetarian, user.isVegetarian)
            set(it.hasHalalRestriction, user.hasHalalRestriction)
            set(it.hasNutsRestriction, user.hasNutsRestriction)
            set(it.hasSeafoodRestriction, user.hasSeafoodRestriction)
            set(it.hasPorkRestriction, user.hasPorkRestriction)
            set(it.hasBeefRestriction, user.hasBeefRestriction)
            set(it.isGlutenIntolerant, user.isGlutenIntolerant)
            set(it.isLactoseIntolerant, user.isLactoseIntolerant)
            set(it.otherRestrictions, user.otherRestrictions)
            set(it.isInactive, user.isInactive)
            set(it.isDeleted, user.isDeleted)
        }

    fun update(uuid: UUID, user: UpdatedUser): Int =
        database.update(Users) {
            set(it.locationUuid, user.locationUuid)
            set(it.isVegetarian, user.isVegetarian)
            set(it.hasHalalRestriction, user.hasHalalRestriction)
            set(it.hasNutsRestriction, user.hasNutsRestriction)
            set(it.hasSeafoodRestriction, user.hasSeafoodRestriction)
            set(it.hasPorkRestriction, user.hasPorkRestriction)
            set(it.hasBeefRestriction, user.hasBeefRestriction)
            set(it.isGlutenIntolerant, user.isGlutenIntolerant)
            set(it.isLactoseIntolerant, user.isLactoseIntolerant)
            set(it.otherRestrictions, user.otherRestrictions)
            where {
                it.uuid eq uuid
            }
        }
    /**
     * Get all attendances for a given schedule.
     * We need a way to know users who have not yet responded if they will be attending. The attendances table is not sufficient to get that information, hence
     * we need to write the below native query using the schedules table, users table and attendances table.
     * @return List of Users
     */
    fun getUsersForUpcomingLunch(): List<User> =
        database.useConnection { connection ->
            val statements = """
                WITH    upcomingScheduleCount as (select count(*) from schedules where date > now() and is_deleted = false),
                        upcomingSchedules as (select * from schedules where date > now() and is_deleted = false),
                        upcomingAttendances as (select distinct on(attendances.user_uuid) * from attendances 
                            where schedule_uuid in (select uuid from upcomingSchedules) and is_attending = true and is_deleted = false),
                        upcomingAttendancesCount as (select count(*) from upcomingAttendances),
                        upcomingUsers as (select *,
                                            (select * from upcomingAttendancesCount) as upac, 
                                            (select * from upcomingScheduleCount) upsc  
                                        from users as u  )
                select * from upcomingUsers where (upac=0 and upsc = 0) or  upac < upsc;
            """.trimIndent()
            connection.prepareStatement(
                statements
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
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
    fun delete(uuid: UUID): Int {
        val result = database.update(Users) {
            set(it.isDeleted, true)
            where {
                it.uuid eq uuid
            }
        }
        // delete related attendances
        database.update(Attendances) {
            set(it.isDeleted, true)
            where {
                it.userUuid eq uuid
            }
        }
        return result
    }
}
