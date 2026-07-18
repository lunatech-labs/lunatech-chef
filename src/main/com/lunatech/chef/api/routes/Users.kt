package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.isAdminSession
import com.lunatech.chef.api.auth.mayManageUser
import com.lunatech.chef.api.auth.respondForbidden
import com.lunatech.chef.api.domain.NewUser
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

data class UpdatedUser(
    val officeUuid: UUID?,
    val isVegetarian: Boolean = false,
    val hasHalalRestriction: Boolean = false,
    val hasNutsRestriction: Boolean = false,
    val hasSeafoodRestriction: Boolean = false,
    val hasPorkRestriction: Boolean = false,
    val hasBeefRestriction: Boolean = false,
    val isGlutenIntolerant: Boolean = false,
    val isLactoseIntolerant: Boolean = false,
    val otherRestrictions: String = "",
    val optOutLunches: Boolean = false,
)

fun Route.users(usersService: UsersService) {
    val usersRoute = "/users"

    route(usersRoute) {
        // get all users, admins only
        get {
            if (!call.isAdminSession) return@get call.respondForbidden()
            val users = usersService.getAll()
            call.respond(OK, users)
        }
        // create a new single users, admins only
        post {
            if (!call.isAdminSession) return@post call.respondForbidden()
            try {
                val newUser = call.receive<NewUser>()
                val inserted = usersService.insert(User.fromNewUser(newUser))
                if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
            } catch (exception: Exception) {
                logger.error("Error creating a new User :( ", exception)
                call.respond(BadRequest, exception.message ?: "")
            }
        }

        route(UUID_ROUTE) {
            // get single user, own profile or admin
            get {
                val uuid =
                    call.parameters[UUID_PARAM].toUUIDOrNull() ?: return@get call.respond(BadRequest, "Invalid UUID")
                if (!call.mayManageUser(uuid)) return@get call.respondForbidden()
                val user = usersService.getByUuid(uuid)
                if (user.isEmpty()) {
                    call.respond(NotFound)
                } else {
                    call.respond(OK, user.first())
                }
            }
            // modify existing user, own profile or admin
            put {
                try {
                    val uuid =
                        call.parameters[UUID_PARAM].toUUIDOrNull() ?: return@put call.respond(
                            BadRequest,
                            "Invalid UUID",
                        )
                    if (!call.mayManageUser(uuid)) return@put call.respondForbidden()
                    val updatedUser = call.receive<UpdatedUser>()
                    val result = usersService.update(uuid, updatedUser)
                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                } catch (exception: Exception) {
                    logger.error("Error updating an User :( ", exception)
                    call.respond(BadRequest, exception.message ?: "")
                }
            }
            // delete a single user, admins only
            delete {
                val uuid =
                    call.parameters[UUID_PARAM].toUUIDOrNull() ?: return@delete call.respond(BadRequest, "Invalid UUID")
                if (!call.isAdminSession) return@delete call.respondForbidden()
                val result = usersService.delete(uuid)
                logger.info("Deleting user {}", result)
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
        }
    }
}
