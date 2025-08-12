package com.lunatech.chef.api.routes

import com.lunatech.chef.api.persistence.services.MenusWithDishesNamesService
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.menusWithDishesInfo(menusWithDishesService: MenusWithDishesNamesService) {
    val menusRoute = "/menusWithDishesInfo"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(menusRoute) {
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
    }
}
