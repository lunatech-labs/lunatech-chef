package com.lunatech.chef.api.auth

import com.lunatech.chef.api.routes.ChefSession
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext
import java.util.UUID

/** True when the call carries a valid session belonging to an admin user. */
val ApplicationCall.isAdminSession: Boolean
    get() = principal<ChefSession>()?.isAdmin == true

/** True when the call's session belongs to [userUuid] or to an admin. */
fun ApplicationCall.mayManageUser(userUuid: UUID): Boolean = isAdminSession || principal<ChefSession>()?.uuid == userUuid

suspend fun ApplicationCall.respondForbidden() = respond(HttpStatusCode.Forbidden, "Administrator rights required")

private val AdminOnlyPlugin =
    createRouteScopedPlugin("AdminOnly") {
        on(AuthenticationChecked) { call ->
            // A missing principal means authentication failed; the session provider's
            // challenge responds with 401, so only authenticated non-admins get a 403.
            val session = call.principal<ChefSession>() ?: return@on
            if (!session.isAdmin) {
                call.respondForbidden()
            }
        }
    }

private val AdminOnlyWritesPlugin =
    createRouteScopedPlugin("AdminOnlyWrites") {
        on(AuthenticationChecked) { call ->
            if (call.request.local.method == HttpMethod.Get) return@on
            val session = call.principal<ChefSession>() ?: return@on
            if (!session.isAdmin) {
                call.respondForbidden()
            }
        }
    }

/**
 * Deliberately relies on identity equality: each instance creates its own child route,
 * so sibling authorization blocks never merge into one route node.
 */
private class AuthorizationRouteSelector(
    private val name: String,
) : RouteSelector() {
    override suspend fun evaluate(
        context: RoutingResolveContext,
        segmentIndex: Int,
    ): RouteSelectorEvaluation = RouteSelectorEvaluation.Transparent

    override fun toString(): String = "(authorize $name)"
}

/**
 * Restricts every route registered inside [build] to admin users.
 * Must be nested inside an authenticate block that produces a [ChefSession] principal.
 */
fun Route.adminOnly(build: Route.() -> Unit): Route {
    val authorizedRoute = createChild(AuthorizationRouteSelector("admin only"))
    authorizedRoute.install(AdminOnlyPlugin)
    authorizedRoute.build()
    return authorizedRoute
}

/**
 * Restricts POST/PUT/DELETE routes registered inside [build] to admin users,
 * while leaving GET routes accessible to any authenticated user.
 * Must be nested inside an authenticate block that produces a [ChefSession] principal.
 */
fun Route.adminOnlyWrites(build: Route.() -> Unit): Route {
    val authorizedRoute = createChild(AuthorizationRouteSelector("admin-only writes"))
    authorizedRoute.install(AdminOnlyWritesPlugin)
    authorizedRoute.build()
    return authorizedRoute
}
