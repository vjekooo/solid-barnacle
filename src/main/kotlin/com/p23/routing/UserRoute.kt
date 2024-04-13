package com.p23.routing

import com.p23.model.User
import com.p23.service.LoginRequest
import com.p23.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
  val email: String,
  val password: String,
)

@Serializable
data class UserResponse(
  val id: Int,
  val email: String,
)

fun Route.userRoute(userService: UserService) {

  get("/{id}") {
    val userRequest = call.parameters["id"]?.toInt()

    val user = if (userRequest != null) {
      userService.findById(userRequest)
    } else null

//    call.response.header(
//      name = "id",
//      value = user?.id ?: UUID.randomUUID().toString(),
//    )
    if (user != null) {
      call.respond(
        message = user.toResponse()
      )
    } else {
      call.respond(HttpStatusCode.NotFound)
    }
  }

//  authenticate {
//    get {
//      val users = userService.findAll()
//
//      call.respond(
//        message = users.map(User::toResponse)
//      )
//    }
//  }

//  authenticate("another-auth") {
//    get("/{id}") {
//      val id: Int = call.parameters["id"]?.toInt()
//        ?: return@get call.respond(HttpStatusCode.BadRequest)
//
//      val foundUser = userService.findById(id)
//        ?: return@get call.respond(HttpStatusCode.NotFound)
//
//      if (foundUser.email != extractPrincipalEmail(call))
//        return@get call.respond(HttpStatusCode.NotFound)
//
//      call.respond(
//        message = foundUser.toResponse()
//      )
//    }
//  }
}

private fun UserRequest.toModel(): User =
  User(
    id = 0,
    email = this.email,
    password = this.password
  )

private fun User.toResponse(): UserResponse =
  UserResponse(
    id = this.id,
    email = this.email,
  )

private fun extractPrincipalEmail(call: ApplicationCall): String? =
  call.principal<JWTPrincipal>()
    ?.payload
    ?.getClaim("email")
    ?.asString()

private fun extractPrincipalUsername(call: ApplicationCall): String? =
  call.principal<JWTPrincipal>()
    ?.payload
    ?.getClaim("username")
    ?.asString()