package com.lunatech.chef.api.routes

import com.lunatech.chef.api.auth.ADMIN_ROLE
import com.lunatech.chef.api.auth.KEYCLOAK_AUTH
import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MeRouteTest {
    private lateinit var usersService: UsersService
    private lateinit var schedulesService: SchedulesService

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        usersService = UsersService(database)
        schedulesService = SchedulesService(database)

        val officesService = OfficesService(database)
        val dishesService = DishesService(database)
        val menusService = MenusService(database)
        val office = anOffice(city = "Rotterdam")
        officesService.insert(office)
        val dish = aDish(name = "Pasta")
        dishesService.insert(dish)
        val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(dish.uuid))
        menusService.insert(menu)
        schedulesService.insert(aSchedule(menuUuid = menu.uuid, officeUuid = office.uuid, date = LocalDate.now().plusDays(7)))
    }

    private fun ApplicationTestBuilder.setupMeRoute() {
        install(ContentNegotiation) {
            register(RouteTestHelpers.jsonContentType, RouteTestHelpers.jacksonConverter())
        }
        installKeycloakAuth(usersService)
        routing {
            authenticate(KEYCLOAK_AUTH) {
                me(usersService)
            }
        }
    }

    @Test
    fun `provisions an unknown user and returns their profile`() =
        testApplication {
            setupMeRoute()
            val client = jsonClient()
            val email = uniqueEmail("fresh")

            val response = client.get("/me") { header(HttpHeaders.Authorization, "Bearer ${accessTokenFor(email)}") }

            assertEquals(HttpStatusCode.OK, response.status)
            val profile = response.body<UserProfile>()
            assertEquals(email, profile.emailAddress)
            assertFalse(profile.isAdmin)
            assertNotNull(usersService.getByEmailAddress(email))
        }

    @Test
    fun `is idempotent for a known user`() =
        testApplication {
            setupMeRoute()
            val client = jsonClient()
            val email = uniqueEmail("repeat")
            val token = accessTokenFor(email)

            val first = client.get("/me") { header(HttpHeaders.Authorization, "Bearer $token") }.body<UserProfile>()
            val second = client.get("/me") { header(HttpHeaders.Authorization, "Bearer $token") }.body<UserProfile>()

            assertEquals(first.uuid, second.uuid)
        }

    @Test
    fun `returns the stored profile for an existing user`() =
        testApplication {
            setupMeRoute()
            val user = aUser(name = "Veggie User", emailAddress = uniqueEmail("veggie"), isVegetarian = true)
            usersService.insert(user)
            val client = jsonClient()

            val response = client.get("/me") { header(HttpHeaders.Authorization, "Bearer ${aUserToken(user)}") }

            val profile = response.body<UserProfile>()
            assertEquals(user.uuid, profile.uuid)
            assertTrue(profile.isVegetarian)
        }

    @Test
    fun `reflects the admin role from the token`() =
        testApplication {
            setupMeRoute()
            val client = jsonClient()

            val response =
                client.get("/me") {
                    header(HttpHeaders.Authorization, "Bearer ${accessTokenFor(uniqueEmail("boss"), roles = listOf(ADMIN_ROLE))}")
                }

            assertTrue(response.body<UserProfile>().isAdmin)
        }

    @Test
    fun `rejects an unauthenticated request`() =
        testApplication {
            setupMeRoute()
            assertEquals(HttpStatusCode.Unauthorized, client.get("/me").status)
        }
}
