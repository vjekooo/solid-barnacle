package com.example

import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
	configureSecurity()
	configureHTTP()
	configureSerialization()
	configureDatabases()
	configureRouting()
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
