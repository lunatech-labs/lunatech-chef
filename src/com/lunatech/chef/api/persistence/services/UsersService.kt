package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.schemas.Users
import com.lunatech.chef.api.routes.UpdatedUser
import java.util.UUID
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where

class UsersService(val database: Database) {
    fun getAll(): List<User> = database.from(Users).select().where { Users.isDeleted eq false }
        .map { Users.createEntity(it) }

    fun getByUuid(uuid: UUID): List<User> =
        database.from(Users).select().where { Users.uuid eq uuid }.map { Users.createEntity(it) }

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

    fun delete(uuid: UUID): Int = database.update(Users) {
        set(it.isDeleted, true)
        where {
            it.uuid eq uuid
        }
    }
}
