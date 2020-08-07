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
private val formatDate = SimpleDateFormat("yyMMddHHmmss")

data class ChefSession(val email: String, val name: String, val isAdmin: Boolean, val ttl: String)
data class AccountPrincipal(val email: String) : Principal

fun Routing.authorization(verifier: GoogleIdTokenVerifier, admins: List<String>) {
    val loginRoute = "/login"
    val tokenRoute = "/{id_token}"
    val tokenParam = "id_token"

    route("$loginRoute$tokenRoute") {
        get {
            val idToken = call.parameters[tokenParam] ?: throw IllegalArgumentException("Error: $tokenParam was not found.")

            try {
                val token = verifier.verify(idToken)
                if (token != null) {
                    val session = buildChefSession(token, admins)
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

fun getUserNameFromEmail(emailAddress: String): String =
    emailAddress
        .split("@")[0]
        .split(".")
        .joinToString(" ") { name -> name.capitalize() }

fun buildChefSession(token: GoogleIdToken, admins: List<String>): ChefSession {
    val payload = token.payload
    val email = payload.email
    val isAdmin = isAdmin(admins, email)

    val ttl = formatDate.format(Date()) ?: throw InternalError("Error adding ttl to ChefSession header.")

    return ChefSession(email = email, name = getUserNameFromEmail(email), isAdmin = isAdmin, ttl = ttl)
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
            AccountPrincipal(session.email)
        }
    } catch (exception: Exception) {
        logger.error("Exception during session validation {}", exception)
        null
    }
}
