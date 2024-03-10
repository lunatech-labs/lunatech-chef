package com.lunatech.chef.api.routes

import com.auth0.jwt.interfaces.Payload
import com.lunatech.chef.api.domain.NewUser
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.application.call
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import mu.KotlinLogging
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}
private val formatDate = SimpleDateFormat("yyMMddHHmmss")

data class ChefSession(
    val ttl: String,
    val isAdmin: Boolean,
    val uuid: UUID,
    val name: String,
    val emailAddress: String,
    val officeUuid: String,
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

data class AccountPrincipal(val email: String) : Principal

fun Routing.authentication(
    schedulesService: SchedulesService,
    attendancesService: AttendancesService,
    usersService: UsersService,
    admins: List<String>,
) {
    val loginRoute = "/login"

    route(loginRoute) {
        authenticate("idtoken") {
            get {
                val payload = call.principal<JWTPrincipal>()?.payload

                if (payload != null) {
                    val user = addUserToDB(schedulesService, attendancesService, usersService, payload)
                    val session = buildChefSession(user, admins)

                    call.sessions.set(session)
                    call.respond(OK, session)
                } else {
                    logger.error("User unauthorized!")
                    call.respond(Unauthorized)
                }
            }
        }
    }
}

fun addUserToDB(
    schedulesService: SchedulesService,
    attendancesService: AttendancesService,
    usersService: UsersService,
    payload: Payload,
): User {
    val email = payload.getClaim("email").asString()
    val user = usersService.getByEmailAddress(email)

    return if (user == null) {
        val name = getUserNameFromEmail(email)
        val newUser = NewUser(name = name, emailAddress = email, officeUuid = null)
        val userToInsert = User.fromNewUser(newUser)
        val inserted = usersService.insert(userToInsert)
        addNewUserToSchedules(schedulesService, attendancesService, userToInsert.uuid)

        if (inserted == 0) logger.error("Error adding new user {}", newUser)

        userToInsert
    } else {
        user
    }
}

fun addNewUserToSchedules(
    schedulesService: SchedulesService,
    attendancesService: AttendancesService,
    userUuid: UUID,
): List<Int> {
    return schedulesService.getAfterDate(LocalDate.now()).map { schedule ->
        attendancesService.insertAttendanceForUser(userUuid, schedule.uuid, null)
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
        officeUuid = user.officeUuid?.toString() ?: "",
        isVegetarian = user.isVegetarian,
        hasHalalRestriction = user.hasHalalRestriction,
        hasNutsRestriction = user.hasNutsRestriction,
        hasSeafoodRestriction = user.hasSeafoodRestriction,
        hasPorkRestriction = user.hasPorkRestriction,
        hasBeefRestriction = user.hasBeefRestriction,
        isGlutenIntolerant = user.isGlutenIntolerant,
        isLactoseIntolerant = user.isLactoseIntolerant,
        otherRestrictions = user.otherRestrictions,
    )
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
