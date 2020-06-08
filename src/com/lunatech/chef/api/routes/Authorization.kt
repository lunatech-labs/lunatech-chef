package com.lunatech.chef.api.routes

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
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
import java.util.concurrent.TimeUnit
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
val formatDate = SimpleDateFormat("yyMMddHHmmss")

data class ChefSession(val userEmail: String, val name: String, val isAdmin: Boolean, val ttl: String)
data class AccountPrincipal(val email: String) : Principal

fun Routing.authorization(verifier: GoogleIdTokenVerifier, usersService: UsersService) {
    val loginRoute = "/login"
    val tokenRoute = "/{id_token}"
    val tokenParam = "id_token"

    route("$loginRoute$tokenRoute") {
        get {
            val idToken = call.parameters[tokenParam] ?: throw IllegalArgumentException("Error: $tokenParam was not found.")

            try {
                val token = verifier.verify(idToken)
                if (token != null) {
                    val session = buildChefSession(token, usersService)
                    call.sessions.set(session)
                    call.respond(OK, session)
                } else {
                    logger.error("User unauthorized!")
                    call.respond(Unauthorized)
                }
            } catch (e: Exception) {
                logger.error("Exception during user login {}", e.toString())
                call.respond(InternalServerError)
            }
        }
    }
}

fun getUserNameFromEmail(emailAddress: String): String =
    emailAddress
        .split("@")[0]
        .split(".")
        .joinToString(" ") { name -> name.capitalize() }

fun buildChefSession(token: GoogleIdToken, usersService: UsersService): ChefSession {
    val payload = token.payload
    val email = payload.email
    val isAdmin = usersService.isAdmin(email)

    val ttl = formatDate.format(Date()) ?: throw InternalError("Error adding ttl to ChefSession header.")

    return ChefSession(userEmail = email, name = getUserNameFromEmail(email), isAdmin = isAdmin, ttl = ttl)
}

fun validateSession(session: ChefSession, ttlLimit: Int): AccountPrincipal? {
    // TODO e quando a route é apenas para admins?
    return try {
        val formatDate = SimpleDateFormat("yyMMddHHmmss")
        val ttlClient: Date = formatDate.parse(session.ttl)!!
        val duration = TimeUnit.MILLISECONDS.toMinutes(Date().time - ttlClient.time)

        if (duration < 0 || duration > ttlLimit) {
            null
        } else {
            AccountPrincipal(session.userEmail)
        }
    } catch (ex: Exception) {
        logger.error("Exception during session validation {}", ex.toString())
        null
    }
}
