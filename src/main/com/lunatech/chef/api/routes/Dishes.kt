package com.lunatech.chef.api.routes

// import com.lunatech.chef.api.auth.rolesAllowed
import com.lunatech.chef.api.domain.Dish
import com.lunatech.chef.api.domain.NewDish
import com.lunatech.chef.api.persistence.services.DishesService
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
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

data class UpdatedDish(
    val name: String,
    val description: String = "",
    val isVegetarian: Boolean = false,
    val isHalal: Boolean = false,
    val hasNuts: Boolean = false,
    val hasSeafood: Boolean = false,
    val hasPork: Boolean = false,
    val hasBeef: Boolean = false,
    val isGlutenFree: Boolean = false,
    val hasLactose: Boolean = false,
)

fun Routing.dishes(dishesService: DishesService) {
    val dishRoute = "/dishes"
    val uuidRoute = "/{uuid}"
    val uuidParam = "uuid"

    route(dishRoute) {
        authenticate("session-auth") {
            // rolesAllowed(Role.ADMIN) {
            // get all dishes
            get {
                val dishes = dishesService.getAll()
                call.respond(OK, dishes)
            }
            // create a new single dish
            post {
                try {
                    val newDish = call.receive<NewDish>()
                    val inserted = dishesService.insert(Dish.fromNewDish(newDish))
                    if (inserted == 1) call.respond(Created) else call.respond(InternalServerError)
                } catch (exception: Exception) {
                    logger.error("Error creating a new Dish :( ", exception)
                    call.respond(BadRequest, exception.message ?: "")
                }
            }

            route(uuidRoute) {
                // get single dish
                get {
                    val uuid = call.parameters[uuidParam]
                    val dish = dishesService.getByUuid(UUID.fromString(uuid))
                    if (dish.isEmpty()) {
                        call.respond(NotFound)
                    } else {
                        call.respond(OK, dish.first())
                    }
                }
                // modify existing dish
                put {
                    try {
                        val uuid = call.parameters[uuidParam]
                        val updatedDish = call.receive<UpdatedDish>()
                        val result = dishesService.update(UUID.fromString(uuid), updatedDish)
                        if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                    } catch (exception: Exception) {
                        logger.error("Error updating a Dish :( ", exception)
                        call.respond(BadRequest, exception.message ?: "")
                    }
                }
                // delete a single dish
                delete {
                    val uuid = call.parameters[uuidParam]
                    val result = dishesService.delete(UUID.fromString(uuid))
                    if (result == 1) call.respond(OK) else call.respond(InternalServerError)
                }
            }
            // }
        }
    }
}
