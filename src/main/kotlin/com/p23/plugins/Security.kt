package com.p23.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
  val audience = environment.config.property("jwt.audience").getString()
  val issuer = environment.config.property("jwt.issuer").getString()
  val secret = environment.config.property("jwt.secret").getString()
  install(Authentication) {
    jwt {
      verifier(
        JWT.require(Algorithm.HMAC512(secret))
          .withAudience(audience)
          .withIssuer(issuer)
          .build()
      )

      validate { credential ->
        if (!credential.payload.getClaim("username").asString().isNullOrEmpty()) {
          JWTPrincipal(credential.payload)
        } else {
          null
        }
      }
    }
  }
}
