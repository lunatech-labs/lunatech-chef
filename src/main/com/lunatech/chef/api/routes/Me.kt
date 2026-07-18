package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.ChefPrincipal
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

/** Profile of the authenticated user, as stored by the frontend at login. */
data class UserProfile(
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
    val optOutLunches: Boolean = false,
) {
    companion object {
        fun fromUser(
            user: User,
            isAdmin: Boolean,
        ): UserProfile =
            UserProfile(
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
                optOutLunches = user.optOutLunches,
            )
    }
}

/**
 * Returns the authenticated user's profile, provisioning the user (and
 * enrolling them into upcoming schedules) on first sight.
 * Must be registered inside an authenticate(KEYCLOAK_AUTH) block.
 */
fun Route.me(usersService: UsersService) {
    route("/me") {
        get {
            val principal = call.principal<ChefPrincipal>()
            if (principal == null) {
                call.respond(Unauthorized)
                return@get
            }
            val user = principal.user ?: usersService.provision(principal.email)
            call.respond(OK, UserProfile.fromUser(user, principal.isAdmin))
        }
    }
}
