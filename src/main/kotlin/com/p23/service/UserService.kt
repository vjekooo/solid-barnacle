package com.p23.service

import com.p23.model.User
import com.p23.repository.UserRepository
import com.p23.routing.UserRequest

class UserService (private val userRepository: UserRepository) {

  suspend fun findById(id: Int): User? = userRepository.readById(id)

  suspend fun findByEmail(email: String): User? = userRepository.readByEmail(email)

  suspend fun save(user: UserRequest): Int? {
    val foundUser = userRepository.readByEmail(user.email)

    return if (foundUser == null) {
      userRepository.create(user)
    } else null
  }
}