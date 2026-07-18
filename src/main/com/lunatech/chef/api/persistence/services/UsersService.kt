package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.NewUser
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.schemas.Attendances
import com.lunatech.chef.api.persistence.schemas.Schedules
import com.lunatech.chef.api.persistence.schemas.Users
import com.lunatech.chef.api.routes.UpdatedUser
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.gte
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

class UsersService(
    private val database: Database,
) {
    fun getAll(): List<User> =
        database
            .from(Users)
            .select()
            .where { Users.isDeleted eq false }
            .map { Users.createEntity(it) }

    fun getByUuid(uuid: UUID): List<User> =
        database
            .from(Users)
            .select()
            .where { Users.uuid eq uuid }
            .map { Users.createEntity(it) }

    fun getByEmailAddress(emailAddress: String): User? =
        database
            .from(Users)
            .select()
            .where { Users.emailAddress eq emailAddress }
            .map { Users.createEntity(it) }
            .firstOrNull()

    fun insert(user: User): Int =
        database.insert(Users) {
            set(it.uuid, user.uuid)
            set(it.name, user.name)
            set(it.emailAddress, user.emailAddress)
            set(it.officeUuid, user.officeUuid)
            set(it.isVegetarian, user.isVegetarian)
            set(it.hasHalalRestriction, user.hasHalalRestriction)
            set(it.hasNutsRestriction, user.hasNutsRestriction)
            set(it.hasSeafoodRestriction, user.hasSeafoodRestriction)
            set(it.hasPorkRestriction, user.hasPorkRestriction)
            set(it.hasBeefRestriction, user.hasBeefRestriction)
            set(it.isGlutenIntolerant, user.isGlutenIntolerant)
            set(it.isLactoseIntolerant, user.isLactoseIntolerant)
            set(it.otherRestrictions, user.otherRestrictions)
            set(it.optOutLunches, user.optOutLunches)
            set(it.isInactive, user.isInactive)
            set(it.isDeleted, user.isDeleted)
        }

    fun update(
        uuid: UUID,
        user: UpdatedUser,
    ): Int =
        database.update(Users) {
            set(it.officeUuid, user.officeUuid)
            set(it.isVegetarian, user.isVegetarian)
            set(it.hasHalalRestriction, user.hasHalalRestriction)
            set(it.hasNutsRestriction, user.hasNutsRestriction)
            set(it.hasSeafoodRestriction, user.hasSeafoodRestriction)
            set(it.hasPorkRestriction, user.hasPorkRestriction)
            set(it.hasBeefRestriction, user.hasBeefRestriction)
            set(it.isGlutenIntolerant, user.isGlutenIntolerant)
            set(it.isLactoseIntolerant, user.isLactoseIntolerant)
            set(it.otherRestrictions, user.otherRestrictions)
            set(it.optOutLunches, user.optOutLunches)
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int {
        database.useTransaction {
            val result =
                database.update(Users) {
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

    /**
     * Returns the user for [emailAddress], creating and enrolling them into all
     * upcoming schedules in a single transaction on first sight. Safe under
     * concurrent first requests: the unique index on email_address makes the
     * loser of the race pick up the winner's row.
     */
    fun provision(emailAddress: String): User =
        getByEmailAddress(emailAddress) ?: try {
            insertWithEnrolment(emailAddress)
        } catch (exception: Exception) {
            getByEmailAddress(emailAddress) ?: throw exception
        }

    private fun insertWithEnrolment(emailAddress: String): User {
        val newUser = NewUser(name = getUserNameFromEmail(emailAddress), emailAddress = emailAddress, officeUuid = null)
        val user = User.fromNewUser(newUser)
        database.useTransaction {
            insert(user)
            val upcomingScheduleUuids =
                database
                    .from(Schedules)
                    .select(Schedules.uuid)
                    .where { Schedules.date gte LocalDate.now() }
                    .map { row -> row[Schedules.uuid]!! }
            upcomingScheduleUuids.forEach { scheduleUuid ->
                database.insert(Attendances) {
                    set(it.uuid, UUID.randomUUID())
                    set(it.scheduleUuid, scheduleUuid)
                    set(it.userUuid, user.uuid)
                    set(it.isAttending, null)
                    set(it.isDeleted, false)
                }
            }
        }
        return user
    }
}

fun getUserNameFromEmail(emailAddress: String): String =
    emailAddress
        .split("@")[0]
        .split(".")
        .joinToString(" ") { name -> name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
