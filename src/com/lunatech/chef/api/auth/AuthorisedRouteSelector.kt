package com.lunatech.chef.api.auth

import io.ktor.application.feature
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.routing.application

/**
 * https://medium.com/@shrikantjagtap99/role-based-authorization-feature-in-ktor-web-framework-in-kotlin-dda88262a86a
 */
class AuthorisedRouteSelector() : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
        RouteSelectorEvaluation.Constant
}

fun Route.rolesAllowed(vararg roles: Role, build: Route.() -> Unit): Route {
    val authorisedRoute = createChild(AuthorisedRouteSelector())
    application.feature(RoleAuthorization).interceptPipeline(this.application, roles.toSet())

    authorisedRoute.build()
    return authorisedRoute
}
