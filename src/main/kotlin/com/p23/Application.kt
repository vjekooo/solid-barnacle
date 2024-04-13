package com.p23

import com.p23.plugins.*
import com.p23.repository.UserRepository
import com.p23.routing.configureRouting
import com.p23.service.JwtService
import com.p23.service.UserService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
	val dbConnection: Connection = connectToPostgres(embedded = false)
	val userRepository = UserRepository(dbConnection)
	val userService = UserService(userRepository)
	val jwtService = JwtService(this, userService)

	configureSecurity()
	configureHTTP()
	configureSerialization()
	configureDatabases()
	configureRouting(jwtService, userService)
	val env = environment.config.propertyOrNull("ktor.environment")?.getString()
	routing {
		get {
			call.respondText(when (env) {
				"dev" -> "Development"
				"prod" -> "Production"
				else -> "..."
			})
		}
	}
}
