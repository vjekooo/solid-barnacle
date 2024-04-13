
import com.p23.routing.UserRequest
import com.p23.service.JwtService
import com.p23.service.LoginRequest
import com.p23.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoute(userService: UserService, jwtService: JwtService) {
	post("/register") {
		val registerRequest = call.receive<UserRequest>()

		val user = userService.save(registerRequest)

		user?.let {
			call.respond(message = mapOf("user" to user))
		} ?: call.respond(HttpStatusCode.BadRequest)

	}
	post("/login") {
		val loginRequest = call.receive<LoginRequest>()

		val token: String? = jwtService.createJwtToken(loginRequest)

		token?.let {
			call.respond(hashMapOf("token" to token))
		} ?: call.respond(
			message = HttpStatusCode.Unauthorized
		)
	}
}