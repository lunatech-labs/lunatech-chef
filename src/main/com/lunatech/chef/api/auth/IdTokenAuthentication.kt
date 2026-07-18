package com.lunatech.chef.api.auth

import com.auth0.jwk.JwkProvider
import com.lunatech.chef.api.config.JwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import java.time.Instant

/**
 * Validates the Keycloak ID token obtained at login, registered as the "idtoken"
 * provider. Only accepts tokens minted for our own client: other clients in the
 * realm share the signature and issuer but not the roles claim semantics.
 */
fun AuthenticationConfig.idTokenAuthentication(
    jwkProvider: JwkProvider,
    jwtConfig: JwtConfig,
) {
    jwt("idtoken") {
        verifier(jwkProvider, jwtConfig.issuer) {
            withAudience(jwtConfig.clientId)
        }
        challenge { _, _ ->
            call.respond(HttpStatusCode.Unauthorized, "IdToken is not valid or has expired")
        }
        validate { credential ->
            if (credential.expiresAt?.toInstant()?.isAfter(Instant.now()) == true &&
                credential.payload.getClaim("email_verified").asBoolean()
            ) {
                JWTPrincipal(credential.payload)
            } else {
                null
            }
        }
    }
}
