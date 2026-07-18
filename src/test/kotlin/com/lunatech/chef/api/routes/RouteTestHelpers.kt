package com.lunatech.chef.api.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lunatech.chef.api.auth.ADMIN_ROLE
import com.lunatech.chef.api.auth.keycloakJwt
import com.lunatech.chef.api.domain.User
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.serialization.jackson.jackson
import io.ktor.server.auth.Authentication
import io.ktor.server.testing.ApplicationTestBuilder
import java.time.Instant

/**
 * Shared test utilities for route tests.
 * Provides consistent Jackson configuration and helper functions.
 */
object RouteTestHelpers {
    /**
     * Creates a configured ObjectMapper with JavaTimeModule and KotlinModule.
     */
    fun configuredObjectMapper(): ObjectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

    /**
     * Creates a configured Jackson converter for use in server ContentNegotiation.
     * Supports LocalDate serialization and Kotlin data classes.
     */
    fun jacksonConverter(): JacksonConverter = JacksonConverter(configuredObjectMapper())

    /**
     * ContentType for JSON requests/responses.
     */
    val jsonContentType: ContentType = ContentType.Application.Json
}

/**
 * Creates an HTTP client configured with Jackson for JSON serialization.
 * Use this for POST/PUT requests that need to serialize request bodies.
 */
fun ApplicationTestBuilder.jsonClient(): HttpClient =
    createClient {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                registerModule(KotlinModule.Builder().build())
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            }
        }
    }

/** Shared secret for HMAC-signed test tokens, mirroring production RS256 verification. */
const val TEST_JWT_SECRET = "test-jwt-secret"
const val TEST_CLIENT_ID = "lunachef-test"
const val TEST_ISSUER = "https://keycloak.test/realms/lunatech"

/**
 * Installs the same "keycloak" JWT authentication the production app uses,
 * with an HMAC verifier enforcing the same audience and issuer checks.
 */
fun ApplicationTestBuilder.installKeycloakAuth(usersService: UsersService) {
    install(Authentication) {
        keycloakJwt(usersService) {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(TEST_JWT_SECRET))
                    .withAudience(TEST_CLIENT_ID)
                    .withIssuer(TEST_ISSUER)
                    .build(),
            )
        }
    }
}

/** Signs a test access token; pass null to omit a claim entirely. */
fun accessTokenFor(
    email: String,
    roles: List<String> = emptyList(),
    audience: String? = TEST_CLIENT_ID,
    issuer: String? = TEST_ISSUER,
    emailVerified: Boolean? = true,
    expiresAt: Instant = Instant.now().plusSeconds(300),
): String {
    val builder =
        JWT
            .create()
            .withClaim("email", email)
            .withClaim("roles", roles)
            .withExpiresAt(expiresAt)
    if (emailVerified != null) builder.withClaim("email_verified", emailVerified)
    if (audience != null) builder.withAudience(audience)
    if (issuer != null) builder.withIssuer(issuer)
    return builder.sign(Algorithm.HMAC256(TEST_JWT_SECRET))
}

/** Builds a valid access token for [user], optionally with the admin role. */
fun aUserToken(
    user: User,
    isAdmin: Boolean = false,
): String = accessTokenFor(user.emailAddress, if (isAdmin) listOf(ADMIN_ROLE) else emptyList())

/** Builds a valid admin token whose email does not belong to any user in the database. */
fun anAdminToken(): String = accessTokenFor("admin@lunatech.nl", listOf(ADMIN_ROLE))

/**
 * Creates a Jackson-configured HTTP client that sends the Bearer [token]
 * with every request. Not suspending: unlike the session variant, no
 * round trip is needed to obtain the credential.
 */
fun ApplicationTestBuilder.authenticatedJsonClient(token: String): HttpClient =
    createClient {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                registerModule(KotlinModule.Builder().build())
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            }
        }
        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
