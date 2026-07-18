package com.lunatech.chef.api.config

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JwtConfigTest {
    @Test
    fun `parses jwt config block`() {
        val config =
            ConfigFactory.parseString(
                """
                jwt {
                  jwkProvider = "https://keycloak.lunatech.com/realms/lunatech/protocol/openid-connect/certs"
                  issuer = "https://keycloak.lunatech.com/realms/lunatech"
                  clientId = "lunachef-local"
                }
                """.trimIndent(),
            )

        val jwtConfig = JwtConfig.fromConfig(config.getConfig("jwt"))

        assertEquals("https://keycloak.lunatech.com/realms/lunatech/protocol/openid-connect/certs", jwtConfig.jwkProvider)
        assertEquals("https://keycloak.lunatech.com/realms/lunatech", jwtConfig.issuer)
        assertEquals("lunachef-local", jwtConfig.clientId)
    }
}
