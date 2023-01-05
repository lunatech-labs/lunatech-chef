package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.domain.MenuWithDishesUuid
import com.lunatech.chef.api.domain.NewMenuWithDishesUuid
import com.lunatech.chef.api.persistence.services.MenusService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class UpdatedMenu(val name: String, val dishesUuids: List<UUID>)

fun Routing.menus(menusService: MenusService) {
    val menusRoute = "/menus"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(menusRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
            // get all menus
            get {
                val menus = menusService.getAll()
                call.respond(OK, menus)
            }
            // create a new single menu
            post {
                try {
                    val newMenu = call.receive<NewMenuWithDishesUuid>()
                    val inserted = menusService.insert(MenuWithDishesUuid.fromNewMenuWithDishesUuid(newMenu))
                    if (inserted == newMenu.dishesUuids.size) call.respond(Created) else call.respond(
                        InternalServerError
                    )
                } catch (exception: Exception) {
                    logger.error("Error creating a Menu :( ", exception)
                    call.respond(BadRequest, exception.message ?: "")
                }
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
                    try {
                        val uuid = call.parameters[uuidParam]
                        val updatedMenu = call.receive<UpdatedMenu>()
                        val result = menusService.update(UUID.fromString(uuid), updatedMenu)
                        if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                    } catch (exception: Exception) {
                        logger.error("Error updating a Menu :( ", exception)
                        call.respond(BadRequest, exception.message ?: "")
                    }
                }
                // delete a single menu
                delete {
                    val uuid = call.parameters[uuidParam]
                    val result = menusService.delete(UUID.fromString(uuid))
                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                }
            }
            // }
        }
    }
}
