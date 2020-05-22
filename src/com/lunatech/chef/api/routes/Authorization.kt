package com.lunatech.chef.api.routes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lunatech.chef.api.config.OauthConfig
import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import io.ktor.sessions.set

class ChefSession(val userId: String)

fun Routing.authorization() {
    authenticate("google-oauth") {
        route("/login") {
            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                    ?: error("No principal")

                val json = HttpClient(Apache).get<String>("https://www.googleapis.com/userinfo/v2/me") {
                    header("Authorization", "Bearer ${principal.accessToken}")
                }

                val data = ObjectMapper().readValue<Map<String, Any?>>(json)
                val id = data["id"] as String?

                if (id != null) {
                    call.sessions.set(ChefSession(id))
                }
                call.respondRedirect("/")
            }
        }
    }

route("/logout") {
    get {
        call.sessions.clear<ChefSession>()
        call.respondRedirect("/login")
    }
}
}
