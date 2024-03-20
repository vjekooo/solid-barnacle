package com.example.plugins

import com.example.routing.UserCredentials
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

import org.mindrot.jbcrypt.BCrypt

object Hasher {
	fun checkPassword(attempt: String, savedPassword: String) = if (BCrypt.checkpw(attempt, savedPassword)) true
		else throw Exception("Wrong Password")
	fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
}

@Serializable
data class User(val name: String, val lastname: String, val email: String, val password: String)
class UserService(private val connection: Connection) {
	companion object {
		private const val CREATE_TABLE_USERS =
			"CREATE TABLE IF NOT EXISTS USERS (ID SERIAL PRIMARY KEY, name VARCHAR(255), lastname VARCHAR(255), email VARCHAR(255), password VARCHAR(255));"
		private const val SELECT_CITY_BY_ID = "SELECT name FROM users WHERE id = ?"
		private const val SELECT_USER_BY_EMAIL = "SELECT name, lastname, email FROM users WHERE email = ?"
		private const val SELECT_USER_BY_CREDENTIALS = "SELECT email, password FROM users WHERE email = ?"
		private const val INSERT_USER = "INSERT INTO users (name, lastname, email, password) VALUES (?, ?, ?, ?)"
		private const val UPDATE_CITY = "UPDATE users SET name = ?, lastname = ?, email = ? WHERE id = ?"
		private const val DELETE_CITY = "DELETE FROM users WHERE id = ?"
	}

	init {
		val statement = connection.createStatement()
		statement.executeUpdate(CREATE_TABLE_USERS)
	}

	// Create a new user
	suspend fun create(user: User): Int = withContext(Dispatchers.IO) {
		val existingUser = readByEmail(user.email)

		if (existingUser.email != null) {
			throw Exception("User with email ${user.email} already exists")
		}

		val statement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)
		statement.setString(1, user.name)
		statement.setString(2, user.lastname)
		statement.setString(3, user.email)

		val hashedPassword = Hasher.hashPassword(user.password)

		statement.setString(4, hashedPassword)
		statement.executeUpdate()

		val generatedKeys = statement.generatedKeys
		if (generatedKeys.next()) {
			return@withContext generatedKeys.getInt(1)
		} else {
			throw Exception("Unable to retrieve the id of the newly inserted user")
		}
	}

	// Read a user by email
	suspend fun readByEmail(email: String): User = withContext(Dispatchers.IO) {
		val statement = connection.prepareStatement(SELECT_USER_BY_EMAIL)
		statement.setString(1, email)
		val resultSet = statement.executeQuery()

		if (resultSet.next()) {
			val name = resultSet.getString("name")
			val lastname = resultSet.getString("lastname")
			return@withContext User(name, lastname, email, password = "")
		} else {
			throw Exception("Record not found")
		}
	}

	// Read a user by id
	suspend fun readById(id: Int): User = withContext(Dispatchers.IO) {
		val statement = connection.prepareStatement(SELECT_CITY_BY_ID)
		statement.setInt(1, id)
		val resultSet = statement.executeQuery()

		if (resultSet.next()) {
			val name = resultSet.getString("name")
			val lastname = resultSet.getString("lastname")
			val email = resultSet.getString("email")
			return@withContext User(name, lastname, email, password = "")
		} else {
			throw Exception("Record not found")
		}
	}

	suspend fun validateUser(credentials: UserCredentials) = withContext(Dispatchers.IO) {
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

	// Update a city
	suspend fun update(id: Int, user: User) = withContext(Dispatchers.IO) {
		val statement = connection.prepareStatement(UPDATE_CITY)
		statement.setString(1, user.name)
		statement.setString(2, user.lastname)
		statement.setInt(3, id)
		statement.executeUpdate()
	}

	// Delete a city
	suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
		val statement = connection.prepareStatement(DELETE_CITY)
		statement.setInt(1, id)
		statement.executeUpdate()
	}
}
