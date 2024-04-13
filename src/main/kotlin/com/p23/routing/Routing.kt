package com.p23.routing

import authRoute
import com.p23.service.JwtService
import com.p23.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting(
	jwtService: JwtService,
	userService: UserService
) {
	install(StatusPages) {
		exception<Throwable> { call, cause ->
			call.respondText(text = "5000: $cause", status = HttpStatusCode.InternalServerError)
		}
	}
	routing {
		get("/") {
			call.respondText("Hello World!")
		}

		route("/api/auth") {
			authRoute(userService, jwtService)
		}

		route("/api/user") {
			userRoute(userService)
		}

		// Static plugin. Try to access `/static/index.html`
		staticFiles("/static", File("files")) {
			extensions("html", "htm")
		}
	}
}
