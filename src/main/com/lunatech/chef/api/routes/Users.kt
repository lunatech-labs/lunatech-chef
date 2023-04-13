package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.lunatech.chef.api.config.JwtConfig
import com.lunatech.chef.api.domain.NewUser
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.sessions.*
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.TimeUnit

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
)

fun Routing.users(usersService: UsersService, jwtConfig: JwtConfig) {
    val usersRoute = "/users"
    val byEmailRoute = "/by-email/{email}"
    val emailParam = "email"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"
    val tokenGeneration = "/token-generation"

    route(usersRoute) {
        authenticate("session-auth", "auth-jwt") {
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
            // get single user by email
            route(byEmailRoute) {
                get {
                    val email = call.parameters[emailParam]
                    val user = usersService.getByEmail(email as String)
                    if (user.isEmpty()) {
                        call.respond(NotFound)
                    } else {
                        call.respond(OK, user.first())
                    }
                }
            }

            // generate token for api usage
            route(tokenGeneration) {
                get {
                    val chefSession = call.sessions.get<ChefSession>()
                    val email = chefSession?.emailAddress

                    if (email != null) {
                        val dbUser = usersService.getByEmail(email)
                        if (dbUser.isEmpty()) {
                            call.respond(NotFound)
                        } else {
                            val user = dbUser.first()
                            val token = JWT.create()
                                .withIssuer(jwtConfig.issuer)
                                .withClaim("username", user.emailAddress)
                                .withExpiresAt(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(jwtConfig.ttlLimit.toLong())))
                                .sign(Algorithm.HMAC256(jwtConfig.secretKey))
                            call.respond(OK, hashMapOf("token" to token))
                        }
                    } else {
                        call.respond(NotFound)
                    }
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
