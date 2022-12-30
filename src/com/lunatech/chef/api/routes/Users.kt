package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.domain.NewUser
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class UpdatedUser(
  val locationUuid: UUID?,
  val isVegetarian: Boolean = false,
  val hasHalalRestriction: Boolean = false,
  val hasNutsRestriction: Boolean = false,
  val hasSeafoodRestriction: Boolean = false,
  val hasPorkRestriction: Boolean = false,
  val hasBeefRestriction: Boolean = false,
  val isGlutenIntolerant: Boolean = false,
  val isLactoseIntolerant: Boolean = false,
  val otherRestrictions: String = ""
)

fun Routing.users(usersService: UsersService) {
    val usersRoute = "/users"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(usersRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
            // get all users
            get {
                val users = usersService.getAll()
                call.respond(OK, users)
            }
            // create a new single users
            post {
                try {
                    val newUser = call.receive<NewUser>()
                    val inserted = usersService.insert(User.fromNewUser(newUser))
                    if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
                } catch (exception: Exception) {
                    logger.error("Error creating a new User :( ", exception)
                    call.respond(BadRequest, exception.message ?: "")
                }
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
                    try {
                        val uuid = call.parameters[uuidParam]
                        val updatedUser = call.receive<UpdatedUser>()
                        val result = usersService.update(UUID.fromString(uuid), updatedUser)
                        if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                    } catch (exception: Exception) {
                        logger.error("Error updating an User :( ", exception)
                        call.respond(BadRequest, exception.message ?: "")
                    }
                }
                // delete a single user
                delete {
                    val uuid = call.parameters[uuidParam]
                    val result = usersService.delete(UUID.fromString(uuid))
                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                }
            }
            // }
        }
    }
}
