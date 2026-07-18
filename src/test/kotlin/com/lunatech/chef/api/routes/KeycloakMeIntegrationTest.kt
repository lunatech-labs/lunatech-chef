package com.lunatech.chef.api.routes

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.lunatech.chef.api.auth.KEYCLOAK_AUTH
import com.lunatech.chef.api.auth.keycloakJwt
import com.lunatech.chef.api.config.JwtConfig
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
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
 * verifies the production access token validation end to end: JWKS signature check,
 * audience check and role extraction.
 */
object TestKeycloak {
    private const val REALM = "lunatech"
    const val CLIENT_ID = "lunachef"

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

    /** Obtains a real access token from Keycloak through the password grant. */
    fun accessTokenFor(username: String): String {
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
        return ObjectMapper().readTree(response.body())["access_token"].asText()
    }
}

class KeycloakMeIntegrationTest {
    private lateinit var usersService: UsersService

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        usersService = UsersService(database)
    }

    private fun ApplicationTestBuilder.setupMeRoute() {
        val jwtConfig = TestKeycloak.jwtConfig()
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        install(Authentication) {
            keycloakJwt(usersService) {
                verifier(JwkProviderBuilder(URI(jwtConfig.jwkProvider).toURL()).build(), jwtConfig.issuer) {
                    withAudience(jwtConfig.clientId)
                }
            }
        }
        routing {
            authenticate(KEYCLOAK_AUTH) {
                me(usersService)
            }
        }
    }

    @Test
    fun `me with a keycloak token from a group member yields an admin profile`() =
        testApplication {
            setupMeRoute()
            val client = jsonClient()

            val response =
                client.get("/me") {
                    header(HttpHeaders.Authorization, "Bearer ${TestKeycloak.accessTokenFor("admin.user@lunatech.nl")}")
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val profile = response.body<UserProfile>()
            assertTrue(profile.isAdmin)
            assertEquals("admin.user@lunatech.nl", profile.emailAddress)
        }

    @Test
    fun `me with a keycloak token from a regular user yields a non-admin profile`() =
        testApplication {
            setupMeRoute()
            val client = jsonClient()

            val response =
                client.get("/me") {
                    header(HttpHeaders.Authorization, "Bearer ${TestKeycloak.accessTokenFor("normal.user@lunatech.nl")}")
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val profile = response.body<UserProfile>()
            assertFalse(profile.isAdmin)
        }

    @Test
    fun `me with a token not signed by keycloak is unauthorized`() =
        testApplication {
            setupMeRoute()
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
                client.get("/me") {
                    header(HttpHeaders.Authorization, "Bearer $forgedToken")
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
}
