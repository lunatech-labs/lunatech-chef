package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.MenuWithDishesUuid
import com.lunatech.chef.api.domain.NewMenuWithDishesUuid
import com.lunatech.chef.api.persistence.services.MenusService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import java.util.UUID

data class UpdatedMenu(val name: String, val dishesUuid: List<UUID>)

fun Routing.menus(menusService: MenusService) {
    val menusRoute = "/menus"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(menusRoute) {
        // get all menus
        get {
            val menus = menusService.getAll()
            call.respond(OK, menus)
        }
        // create a new single menu
        post {
            val newMenu = call.receive<NewMenuWithDishesUuid>()
            val inserted = menusService.insert(MenuWithDishesUuid.fromNewMenuWithDishesUuid(newMenu))
            if (inserted == newMenu.dishesUuid.size) call.respond(Created) else call.respond(InternalServerError)
        }

        route(uuidRoute) {
            // get single menu
            get {
                val uuid = call.parameters[uuidParam]
                val menu = menusService.getByUuid(UUID.fromString(uuid))

                if (menu == null) {
                    call.respond(NotFound)
                }
                menu?.let { call.respond(OK, it) }
            }
            // modify existing menu
            put {
                val uuid = call.parameters[uuidParam]
                val updatedMenu = call.receive<UpdatedMenu>()
                val result = menusService.update(UUID.fromString(uuid), updatedMenu)
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
            // delete a single menu
            delete {
                val uuid = call.parameters[uuidParam]
                val result = menusService.delete(UUID.fromString(uuid))
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
        }
    }
}
