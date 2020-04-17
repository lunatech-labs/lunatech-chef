package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import java.util.UUID

data class UpdatedUser(
    val name: String,
    val emailAddress: String,
    val isAdmin: Boolean,
    val location: UUID,
    val isVegetarian: Boolean = false,
    val hasNutsRestriction: Boolean = false,
    val hasSeafoodRestriction: Boolean = false,
    val hasPorkRestriction: Boolean = false,
    val hasBeefRestriction: Boolean = false,
    val isGlutenIntolerant: Boolean = false,
    val isLactoseIntolerant: Boolean = false,
    val isInactive: Boolean = false,
    val otherRestriction: String = ""
)

fun Routing.users(usersService: UsersService) {
    val usersRoute = "/users"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(usersRoute) {
        // get all users
        get {
            val users = usersService.getAll()
            call.respond(OK, users)
        }
        // create a new single users
        post {
            val newUser = call.receive<User>()
            val inserted = usersService.insert(newUser)
            if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
        }

        route(uuidRoute) {
            // get single user
            get {
                val uuid = call.parameters[uuidParam]
                val user = usersService.getByUuid(UUID.fromString(uuid))
                if (user.isEmpty()) {
                    call.respond(NotFound)
                } else {
                    call.respond(OK, user.first())
                }
            }
            // modify existing user
            put {
                val uuid = call.parameters[uuidParam]
                val updatedUser = call.receive<UpdatedUser>()
                val result = usersService.update(UUID.fromString(uuid), updatedUser)
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
            // delete a single user
            delete {
                val uuid = call.parameters[uuidParam]
                val result = usersService.delete(UUID.fromString(uuid))
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
        }
    }
}
