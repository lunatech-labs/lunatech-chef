package com.lunatech.chef.api.routes

import com.lunatech.chef.api.domain.DishOnMenu
import com.lunatech.chef.api.persistence.services.DishesOnMenusService
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

data class UpdatedDishOnMenu(val menuUuid: UUID, val dishUuid: UUID)

fun Routing.dishesOnMenus(dishesOnMenusService: DishesOnMenusService) {
    val dishOnMenuRoute = "/dishesonmenus"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(dishOnMenuRoute) {
        // get all dishes on menus
        get {
            val dishesMenus = dishesOnMenusService.getAll()
            call.respond(OK, dishesMenus)
        }
        // create a new single dish on a many
        post {
            val newDishMenu = call.receive<DishOnMenu>()
            val inserted = dishesOnMenusService.insert(newDishMenu)
            if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
        }

        route(uuidRoute) {
            // get single dish on menu
            get {
                val uuid = call.parameters[uuidParam]
                val dishMenu = dishesOnMenusService.getByUuid(UUID.fromString(uuid))
                if (dishMenu.isEmpty()) {
                    call.respond(NotFound)
                } else {
                    call.respond(OK, dishMenu.first())
                }
            }
            // modify existing dish on menu
            put {
                val uuid = call.parameters[uuidParam]
                val updatedDishMenu = call.receive<UpdatedDishOnMenu>()
                val result = dishesOnMenusService.update(UUID.fromString(uuid), updatedDishMenu)
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
            // delete a single dish on menu
            delete {
                val uuid = call.parameters[uuidParam]
                val result = dishesOnMenusService.delete(UUID.fromString(uuid))
                if (result == 1) call.respond(OK) else call.respond(InternalServerError)
            }
        }
    }
}
