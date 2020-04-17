package com.lunatech.chef.api.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.healthCheck() {
    get("/health-check") {
        // Check databases/other services.
        call.respond(OK)
    }
}
