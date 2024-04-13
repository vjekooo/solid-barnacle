package com.p23.repository

import com.p23.model.User
import com.p23.routing.UserRequest
import com.p23.service.LoginRequest
import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.Statement

import org.mindrot.jbcrypt.BCrypt

object Hasher {
	fun checkPassword(attempt: String, savedPassword: String) = if (BCrypt.checkpw(attempt, savedPassword)) true
		else throw Exception("Wrong Password")
	fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
}

class UserRepository(private val connection: Connection) {

	companion object {
		private const val CREATE_TABLE_USERS =
			"CREATE TABLE IF NOT EXISTS USERS (ID SERIAL PRIMARY KEY, email VARCHAR(255), password VARCHAR(255));"
		private const val SELECT_USER_BY_ID = "SELECT * FROM users WHERE id = ?"
		private const val SELECT_USER_BY_EMAIL = "SELECT * FROM users WHERE email = ?"
		private const val SELECT_USER_BY_CREDENTIALS = "SELECT email, password FROM users WHERE email = ?"
		private const val INSERT_USER = "INSERT INTO users (email, password) VALUES (?, ?)"
	}

	init {
		val statement = connection.createStatement()
		statement.executeUpdate(CREATE_TABLE_USERS)
	}

	// Create a new user
	suspend fun create(user: UserRequest): Int = withContext(Dispatchers.IO) {
		val existingUser = readByEmail(user.email)

		if (existingUser?.email != null) {
			throw Exception("User with email ${user.email} already exists")
		}

		val statement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)
		statement.setString(1, user.email)

		val hashedPassword = Hasher.hashPassword(user.password)

		statement.setString(2, hashedPassword)
		statement.executeUpdate()

		val generatedKeys = statement.generatedKeys
		if (generatedKeys.next()) {
			return@withContext generatedKeys.getInt(1)
		} else {
			throw Exception("Unable to retrieve the id of the newly inserted user")
		}
	}

	// Read a user by email
	suspend fun readByEmail(email: String): User? = withContext(Dispatchers.IO) {
		val statement = connection.prepareStatement(SELECT_USER_BY_EMAIL)
		statement.setString(1, email)
		val resultSet = statement.executeQuery()

		if (resultSet.next()) {
			val id = resultSet.getInt("id")
			val password = resultSet.getString("password")
			return@withContext User(id, email, password)
		} else {
			return@withContext null
		}
	}

	suspend fun readById(id: Int): User = withContext(Dispatchers.IO) {
		val statement = connection.prepareStatement(SELECT_USER_BY_ID)
		statement.setInt(1, id)
		val resultSet = statement.executeQuery()

		if (resultSet.next()) {
			val email = resultSet.getString("email")
			return@withContext User(id, email, "")
		} else {
			throw Exception("Record not found")
		}
	}

	suspend fun validateUser(credentials: LoginRequest) = withContext(Dispatchers.IO) {
		val statement = connection.prepareStatement(SELECT_USER_BY_CREDENTIALS, Statement.RETURN_GENERATED_KEYS)

		statement.setString(1, credentials.email)
		val result = statement.executeQuery()

		if (result.next()) {
			Hasher.checkPassword(credentials.password, result.getString("password"))

			// Do stuff

		} else {
			throw Exception("Invalid email of password")
		}
	}
}
