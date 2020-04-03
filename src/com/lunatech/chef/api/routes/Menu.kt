package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.Menu
import com.lunatech.chef.api.persistence.services.MenusService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import java.util.UUID

data class UpdatedMenu(val name: String)

fun Routing.menus(menusService: MenusService){
    val menuRoute = "/menus"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(menuRoute) {
        // get all menus
        get {
            val menus = menusService.getAll()
            call.respond(HttpStatusCode.OK, menus)
        }
        // create a new single menu
        post {
            val newMenu = call.receive<Menu>()
            val inserted = menusService.insert(newMenu)
            if(inserted == 1) call.respond(HttpStatusCode.Created) else call.respond(HttpStatusCode.InternalServerError)
        }

        route(uuidRoute) {
            // get single menu
            get {
                val uuid = call.parameters[uuidParam]
                val menu = menusService.getByUuid(UUID.fromString(uuid))
                if (menu.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(HttpStatusCode.OK, menu.first())
                }
            }
            // modify existing menu
            put {
                val uuid = call.parameters[uuidParam]
                val updatedMenu = call.receive<UpdatedMenu>()
                val result = menusService.update(UUID.fromString(uuid), updatedMenu)
                if (result == 1) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
            }
            // delete a single menu
            delete {
                val uuid = call.parameters[uuidParam]
                val result = menusService.delete(UUID.fromString(uuid))
                if (result == 1) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
