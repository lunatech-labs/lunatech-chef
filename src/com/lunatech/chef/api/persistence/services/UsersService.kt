package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.schemas.Users
import com.lunatech.chef.api.routes.UpdatedUser
import java.util.UUID
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.dsl.where

class UsersService(val database: Database) {
    fun getAll() = database.from(Users).select().where { Users.isDeleted eq false }.map { Users.createEntity(it) }

    fun getByUuid(uuid: UUID): List<User> =
        database.from(Users).select().where { -> Users.uuid eq uuid }.map { Users.createEntity(it) }

    fun insert(user: User): Int =
        database.insert(Users) {
            it.uuid to user.uuid
            it.name to user.name
            it.emailAddress to user.emailAddress
            it.isAdmin to user.isAdmin
            it.location to user.location
            it.isVegetarian to user.isVegetarian
            it.hasNutsRestriction to user.hasNutsRestriction
            it.hasSeafoodRestriction to user.hasSeafoodRestriction
            it.hasPorkRestriction to user.hasPorkRestriction
            it.hasBeefRestriction to user.hasBeefRestriction
            it.isGlutenIntolerant to user.isGlutenIntolerant
            it.isLactoseIntolerant to user.isLactoseIntolerant
            it.otherRestriction to user.otherRestriction
            it.isInactive to user.isInactive
            it.isDeleted to user.isDeleted
        }

    fun update(uuid: UUID, user: UpdatedUser): Int =
        database.update(Users) {
            it.name to user.name
            it.emailAddress to user.emailAddress
            it.isAdmin to user.isAdmin
            it.location to user.location
            it.isVegetarian to user.isVegetarian
            it.hasNutsRestriction to user.hasNutsRestriction
            it.hasSeafoodRestriction to user.hasSeafoodRestriction
            it.hasPorkRestriction to user.hasPorkRestriction
            it.hasBeefRestriction to user.hasBeefRestriction
            it.isGlutenIntolerant to user.isGlutenIntolerant
            it.isLactoseIntolerant to user.isLactoseIntolerant
            it.otherRestriction to user.otherRestriction
            it.isInactive to user.isInactive
            where {
                it.uuid eq uuid
            }
        }

    fun delete(uuid: UUID): Int = database.update(Users) {
        it.isDeleted to true
        where {
            it.uuid eq uuid
        }
    }
}
