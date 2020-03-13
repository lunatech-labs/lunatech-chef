package com.lunatech.chef.api.routes

import io.ktor.routing.Routing
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get

fun Routing.healthCheck(){
    get("/health-check") {
        // Check databases/other services.
        call.respondText("OK")
    }
}
