package com.lunatech.chef.api.routes

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HealthCheckRoutesTest {
    @Nested
    inner class HealthEndpoint {
        @Test
        fun `returns OK status`() =
            testApplication {
                routing { healthCheck() }

                val response = client.get("/health-check")

                assertEquals(HttpStatusCode.OK, response.status)
            }
    }
}
