package com.lunatech.chef.api.routes

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.lunatech.chef.api.ChefSession
import io.ktor.application.call
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
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun getUserNameFromEmail(emailAddress: String) =
    emailAddress.split("@")[0].split(".").joinToString(" ") { name -> name.capitalize() }

fun Routing.authorization(verifier: GoogleIdTokenVerifier) {
    val loginRoute = "/login"
    val tokenRoute = "/{id_token}"
    val tokenParam = "id_token"

    route("$loginRoute$tokenRoute") {
        get {
            val idToken = call.parameters[tokenParam]
            idToken ?: throw IllegalArgumentException("$tokenParam is not found")

            try {
                val token = verifier.verify(idToken.toString())
                if (token != null) {

                    logger.info(token.toString())
                    val payload = token.payload
                    val email = payload.email

                    call.sessions.set(ChefSession(userEmail = email, name = getUserNameFromEmail(email), isAdmin = true))
                    call.respond(OK)
                } else {
                    logger.error("User unauthorized!")
                    call.respond(Unauthorized)
                }
            } catch (e: Exception) {
                logger.error("Exception while calling GoogleIdTokenVerifier {}", e.toString())
            }
        }
    }
}
