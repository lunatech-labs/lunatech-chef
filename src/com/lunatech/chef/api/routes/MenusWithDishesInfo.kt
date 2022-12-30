package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

fun Routing.menusWithDishesInfo(menusWithDishesService: MenusWithDishesNamesService) {
    val menusRoute = "/menusWithDishesInfo"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(menusRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
            // get all menus with the complete data about the dishes
            get {
                val menus = menusWithDishesService.getAll()
                call.respond(OK, menus)
            }

            route(uuidRoute) {
                // get single menu with the complete data about the dishes
                get {
                    val uuid = call.parameters[uuidParam]
                    val menu = menusWithDishesService.getByUuid(UUID.fromString(uuid))

                    if (menu == null) {
                        call.respond(NotFound)
                    }
                    menu?.let { call.respond(OK, it) }
                }
            }
            // }
        }
    }
}
