package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.Location
import com.lunatech.chef.api.persistence.schemas.Locations
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.insert

fun Routing.locations(database: Database) {
    post("/locations") {
        val newLocation = call.receive<Location>()

        val result = database.insert(Locations) {
            it.uuid to newLocation.uuid
            it.city to newLocation.city
            it.country to newLocation.country
            it.isDeleted to newLocation.isDeleted
        }

        if (result == 1) call.respond(HttpStatusCode.Created) else call.respond(HttpStatusCode.InternalServerError)
    }
}
