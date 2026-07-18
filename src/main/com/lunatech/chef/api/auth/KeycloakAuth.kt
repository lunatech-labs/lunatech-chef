package com.lunatech.chef.api.auth

import com.auth0.jwt.interfaces.Payload
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Name of the Keycloak JWT authentication provider. */
const val KEYCLOAK_AUTH = "keycloak"

private const val ROLES_CLAIM = "roles"
private const val EMAIL_CLAIM = "email"
private const val EMAIL_VERIFIED_CLAIM = "email_verified"

/**
 * Principal for a request carrying a verified Keycloak access token.
 * [user] is null until the user is provisioned via GET /me; ownership
 * checks fail closed on a null user.
 */
data class ChefPrincipal(
    val user: User?,
    val email: String,
    val isAdmin: Boolean,
)

/**
 * Extracts the roles claim from the verified token payload. A missing or
 * malformed claim yields an empty list, so callers fail closed to non-admin.
 */
fun extractRoles(payload: Payload): List<String> =
    try {
        payload.getClaim(ROLES_CLAIM).asList(String::class.java) ?: emptyList()
    } catch (exception: Exception) {
        logger.debug("Could not read the $ROLES_CLAIM claim from the access token {}", exception)
        emptyList()
    }

/**
 * Builds the request principal from a signature-verified token payload:
 * requires a verified email and resolves the user by email address.
 */
fun validateKeycloakToken(
    payload: Payload,
    usersService: UsersService,
): ChefPrincipal? {
    if (payload.getClaim(EMAIL_VERIFIED_CLAIM).asBoolean() != true) return null
    val email = payload.getClaim(EMAIL_CLAIM).asString() ?: return null
    val isAdmin = ADMIN_ROLE in extractRoles(payload)

    return ChefPrincipal(user = usersService.getByEmailAddress(email), email = email, isAdmin = isAdmin)
}

/**
 * Registers the "keycloak" JWT provider. The verifier (JWKS in production,
 * HMAC in tests) is supplied by [configureVerifier]; validation and the 401
 * challenge are shared so tests exercise the production logic.
 */
fun AuthenticationConfig.keycloakJwt(
    usersService: UsersService,
    configureVerifier: JWTAuthenticationProvider.Config.() -> Unit,
) {
    jwt(KEYCLOAK_AUTH) {
        configureVerifier()
        validate { credential -> validateKeycloakToken(credential.payload, usersService) }
        challenge { _, _ ->
            call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
        }
    }
}
