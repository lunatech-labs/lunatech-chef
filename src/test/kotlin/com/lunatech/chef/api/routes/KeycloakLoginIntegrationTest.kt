package com.lunatech.chef.api.routes

import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.lunatech.chef.api.auth.idTokenAuthentication
import com.lunatech.chef.api.config.JwtConfig
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.Authentication
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.header
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Runs the real Keycloak from the local dev realm export (dockerdev/keycloak) and
 * verifies the production ID token validation end to end: JWKS signature check,
 * audience check and role extraction.
 */
object TestKeycloak {
    private const val REALM = "lunatech"
    const val CLIENT_ID = "lunachef-local"

    private val container: GenericContainer<*> by lazy {
        GenericContainer(DockerImageName.parse("quay.io/keycloak/keycloak:26.3"))
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
            .withCommand("start-dev", "--import-realm")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("lunatech-realm.json"),
                "/opt/keycloak/data/import/lunatech-realm.json",
            ).withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/realms/$REALM/.well-known/openid-configuration").forPort(8080))
            .also { it.start() }
    }

    private val realmUrl: String
        get() = "http://${container.host}:${container.getMappedPort(8080)}/realms/$REALM"

    fun jwtConfig(): JwtConfig =
        JwtConfig(
            jwkProvider = "$realmUrl/protocol/openid-connect/certs",
            issuer = realmUrl,
            clientId = CLIENT_ID,
        )

    /** Obtains a real ID token from Keycloak through the password grant. */
    fun idTokenFor(username: String): String {
        val form =
            mapOf(
                "client_id" to CLIENT_ID,
                "grant_type" to "password",
                "username" to username,
                "password" to "lunachef",
                "scope" to "openid",
            ).entries.joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, Charsets.UTF_8)}" }
        val request =
            HttpRequest
                .newBuilder(URI("$realmUrl/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() == 200) { "Token request failed: ${response.body()}" }
        return ObjectMapper().readTree(response.body())["id_token"].asText()
    }
}

class KeycloakLoginIntegrationTest {
    private lateinit var schedulesService: SchedulesService
    private lateinit var attendancesService: AttendancesService
    private lateinit var usersService: UsersService

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        schedulesService = SchedulesService(database)
        usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService)
    }

    private fun ApplicationTestBuilder.setupLoginRoute() {
        val jwtConfig = TestKeycloak.jwtConfig()
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        install(Sessions) {
            header<ChefSession>(TEST_SESSION_HEADER) {
                transform(SessionTransportTransformerMessageAuthentication("test-session-secret".encodeToByteArray()))
            }
        }
        install(Authentication) {
            idTokenAuthentication(UrlJwkProvider(URI(jwtConfig.jwkProvider).toURL()), jwtConfig)
        }
        routing {
            authentication(schedulesService, attendancesService, usersService)
        }
    }

    @Test
    fun `login with a keycloak token from a group member yields an admin session`() =
        testApplication {
            setupLoginRoute()
            val client = jsonClient()

            val response =
                client.get("/login") {
                    header(HttpHeaders.Authorization, "Bearer ${TestKeycloak.idTokenFor("admin.user@lunatech.nl")}")
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val session = response.body<ChefSession>()
            assertTrue(session.isAdmin)
            assertEquals("admin.user@lunatech.nl", session.emailAddress)
        }

    @Test
    fun `login with a keycloak token from a regular user yields a non-admin session`() =
        testApplication {
            setupLoginRoute()
            val client = jsonClient()

            val response =
                client.get("/login") {
                    header(HttpHeaders.Authorization, "Bearer ${TestKeycloak.idTokenFor("normal.user@lunatech.nl")}")
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val session = response.body<ChefSession>()
            assertFalse(session.isAdmin)
        }

    @Test
    fun `login with a token not signed by keycloak is unauthorized`() =
        testApplication {
            setupLoginRoute()
            val client = jsonClient()
            val forgedToken =
                JWT
                    .create()
                    .withIssuer(TestKeycloak.jwtConfig().issuer)
                    .withAudience(TestKeycloak.CLIENT_ID)
                    .withClaim("email", "intruder@lunatech.nl")
                    .withClaim("email_verified", true)
                    .withClaim("roles", listOf("admin"))
                    .sign(Algorithm.HMAC256("not-the-keycloak-key"))

            val response =
                client.get("/login") {
                    header(HttpHeaders.Authorization, "Bearer $forgedToken")
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
}
