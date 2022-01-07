package com.lunatech.chef.api.routes

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.lunatech.chef.api.domain.NewUser
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.application.call
import io.ktor.auth.Principal
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
private val formatDate = SimpleDateFormat("yyMMddHHmmss")

data class ChefSession(
  val ttl: String,
  val isAdmin: Boolean,
  val uuid: UUID,
  val name: String,
  val emailAddress: String,
  val locationUuid: UUID?,
  val isVegetarian: Boolean = false,
  val hasNutsRestriction: Boolean = false,
  val hasSeafoodRestriction: Boolean = false,
  val hasPorkRestriction: Boolean = false,
  val hasBeefRestriction: Boolean = false,
  val isGlutenIntolerant: Boolean = false,
  val isLactoseIntolerant: Boolean = false,
  val otherRestrictions: String = ""
)

data class AccountPrincipal(val email: String) : Principal

fun Routing.authorization(usersService: UsersService, verifier: GoogleIdTokenVerifier, admins: List<String>) {
    val loginRoute = "/login"
    val tokenRoute = "/{id_token}"
    val tokenParam = "id_token"

    route("$loginRoute$tokenRoute") {
        get {
            val idToken = call.parameters[tokenParam] ?: throw IllegalArgumentException("Error: $tokenParam was not found.")

            try {
                val token = verifier.verify(idToken)
                if (token != null) {
                    val user = addUserToDB(usersService, token)
                    val session = buildChefSession(user, admins)

                    call.sessions.set(session)
                    call.respond(OK, session)
                } else {
                    logger.error("User unauthorized!")
                    call.respond(Unauthorized)
                }
            } catch (exception: Exception) {
                logger.error("Exception occurred during user login {}", exception.toString())
                call.respond(InternalServerError, exception.message ?: "")
            }
        }
    }
}

fun addUserToDB(usersService: UsersService, token: GoogleIdToken): User {
    val payload = token.payload
    val email = payload.email
    val user = usersService.getByEmailAddress(email)
    val name = getUserNameFromEmail(email)

    return if (user == null) {
        val newUser = NewUser(name = name, emailAddress = email, locationUuid = null)
        val userToInsert = User.fromNewUser(newUser)
        val inserted = usersService.insert(userToInsert)

        if (inserted == 0) logger.error("Error adding new user {}", newUser)

        userToInsert
    } else {
        user
    }
}

fun getUserNameFromEmail(emailAddress: String): String =
    emailAddress
        .split("@")[0]
        .split(".")
        .joinToString(" ") { name -> name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }

fun buildChefSession(user: User, admins: List<String>): ChefSession {
    val isAdmin = isAdmin(admins, user.emailAddress)
    val ttl = formatDate.format(Date()) ?: throw InternalError("Error adding ttl to ChefSession header.")

    return ChefSession(
        ttl = ttl,
        isAdmin = isAdmin,
        uuid = user.uuid,
        name = user.name,
        emailAddress = user.emailAddress,
        locationUuid = user.locationUuid,
        isVegetarian = user.isVegetarian,
        hasNutsRestriction = user.hasNutsRestriction,
        hasSeafoodRestriction = user.hasSeafoodRestriction,
        hasPorkRestriction = user.hasPorkRestriction,
        hasBeefRestriction = user.hasBeefRestriction,
        isGlutenIntolerant = user.isGlutenIntolerant,
        isLactoseIntolerant = user.isLactoseIntolerant,
        otherRestrictions = user.otherRestrictions)
}

fun isAdmin(admins: List<String>, email: String): Boolean = admins.contains(email)

fun validateSession(session: ChefSession, ttlLimit: Int): AccountPrincipal? {
    return try {
        val formatDate = SimpleDateFormat("yyMMddHHmmss")
        val ttlClient: Date = formatDate.parse(session.ttl)!!
        val duration = TimeUnit.MILLISECONDS.toMinutes(Date().time - ttlClient.time)

        if (duration < 0 || duration > ttlLimit) {
            null
        } else {
            AccountPrincipal(session.emailAddress)
        }
    } catch (exception: Exception) {
        logger.error("Exception during session validation {}", exception)
        null
    }
}
