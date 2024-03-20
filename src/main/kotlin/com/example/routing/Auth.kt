package com.example.routing
import com.example.plugins.User
import com.example.plugins.UserService
import com.example.plugins.connectToPostgres
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.sql.Connection

@Serializable
data class UserCredentials(val email: String, val password: String)

fun Application.authenticationRoutes() {
	val dbConnection: Connection = connectToPostgres(embedded = false)
	val userService = UserService(dbConnection)

	routing {
		post("/register") {
			val user = call.receive<User>()
			val id = userService.create(user)
			call.respond(HttpStatusCode.Created, id)
		}

		post("/login") {
			val credentials = call.receive<UserCredentials>()
			val id = userService.validateUser(credentials)
			call.respond(HttpStatusCode.OK, id)
		}
	}
}

