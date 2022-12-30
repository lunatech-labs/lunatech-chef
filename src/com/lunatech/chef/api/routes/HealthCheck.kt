package com.lunatech.chef.api.routes

import io.ktor.server.application.call
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.healthCheck() {
    get("/health-check") {
        // Check databases/other services.
        call.respond(OK)
    }
}
