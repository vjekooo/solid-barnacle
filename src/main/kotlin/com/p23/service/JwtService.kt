package com.p23.service
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.p23.model.User
import com.p23.repository.Hasher
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class LoginRequest (
  var email: String,
  var password: String
)

class JwtService(
  private val application: Application,
  private val userService: UserService,
) {

  private val secret = getConfigProperty("jwt.secret")
  private val issuer = getConfigProperty("jwt.issuer")
  private val audience = getConfigProperty("jwt.audience")

  val jwtVerifier: JWTVerifier =
    JWT
      .require(Algorithm.HMAC256(secret))
      .withAudience(audience)
      .withIssuer(issuer)
      .build()

  suspend fun createJwtToken(loginRequest: LoginRequest): String? {
    val foundUser: User? = userService.findByEmail(loginRequest.email)

    val isValidPassword = if (loginRequest?.password != null && foundUser?.password != null) {
      Hasher.checkPassword(loginRequest.password, foundUser.password)
    } else false

    return if (isValidPassword)
      JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("email", loginRequest.email)
        .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
        .sign(Algorithm.HMAC256(secret))
    else
      null
  }

  suspend fun customValidator(
    credential: JWTCredential,
  ): JWTPrincipal? {
    val email: String? = extractUsername(credential)
    val foundUser: User? = if (email != null) {
      userService.findByEmail(email)
    } else null


    return foundUser?.let {
      if (audienceMatches(credential))
        JWTPrincipal(credential.payload)
      else
        null
    }
  }

  private fun audienceMatches(
    credential: JWTCredential,
  ): Boolean =
    credential.payload.audience.contains(audience)

  private fun getConfigProperty(path: String) =
    application.environment.config.property(path).getString()

  private fun extractUsername(credential: JWTCredential): String? =
    credential.payload.getClaim("email").asString()
}