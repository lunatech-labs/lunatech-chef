package com.lunatech.chef.api.routes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lunatech.chef.api.domain.User
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.serialization.jackson.jackson
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.session
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.header
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.testing.ApplicationTestBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

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

/** Name of the session header, mirroring the production configuration. */
const val TEST_SESSION_HEADER = "CHEF_SESSION"

private const val TEST_SESSION_SECRET = "test-session-secret"
private val ttlFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmss")

/**
 * Installs the same header-based session authentication the production app uses,
 * registered under the "session-auth" provider name.
 */
fun ApplicationTestBuilder.installSessionAuth() {
    install(Sessions) {
        header<ChefSession>(TEST_SESSION_HEADER) {
            transform(SessionTransportTransformerMessageAuthentication(TEST_SESSION_SECRET.encodeToByteArray()))
        }
    }
    install(Authentication) {
        session<ChefSession>("session-auth") {
            validate { session -> validateSession(session, ttlLimit = 960) }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Session is not valid or has expired")
            }
        }
    }
}

/**
 * Test-only route that turns a posted [ChefSession] into a signed session header,
 * so tests can authenticate without going through the full OIDC login flow.
 */
fun Route.testSessionIssuer() {
    post("/test-session") {
        val session = call.receive<ChefSession>()
        call.sessions.set(session)
        call.respond(HttpStatusCode.OK)
    }
}

/** Builds a valid, freshly-stamped session for [user]. */
fun aChefSession(
    user: User,
    isAdmin: Boolean = false,
): ChefSession =
    ChefSession(
        ttl = LocalDateTime.now().format(ttlFormatter),
        isAdmin = isAdmin,
        uuid = user.uuid,
        name = user.name,
        emailAddress = user.emailAddress,
        officeUuid = user.officeUuid?.toString() ?: "",
    )

/** Posts [session] to the test issuer route and returns the signed session header value. */
suspend fun HttpClient.obtainSessionHeader(session: ChefSession): String {
    val response =
        post("/test-session") {
            contentType(RouteTestHelpers.jsonContentType)
            setBody(session)
        }
    return response.headers[TEST_SESSION_HEADER]
        ?: error("Test session issuer did not return a $TEST_SESSION_HEADER header")
}

/** Builds a valid admin session that does not belong to any user in the database. */
fun anAdminSession(): ChefSession =
    ChefSession(
        ttl = LocalDateTime.now().format(ttlFormatter),
        isAdmin = true,
        uuid = UUID.randomUUID(),
        name = "Admin",
        emailAddress = "admin@lunatech.nl",
        officeUuid = "",
    )

/**
 * Creates a Jackson-configured HTTP client that sends the signed session header
 * for [session] with every request.
 */
suspend fun ApplicationTestBuilder.authenticatedJsonClient(session: ChefSession): HttpClient {
    val headerValue = jsonClient().obtainSessionHeader(session)
    return createClient {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                registerModule(KotlinModule.Builder().build())
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            }
        }
        defaultRequest {
            header(TEST_SESSION_HEADER, headerValue)
        }
    }
}
