package org.halflife.clientmanager.repository

import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.model.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(
    private val encoder: PasswordEncoder
) {

    private val users = mutableListOf(
        User(UUID.randomUUID(), email = "sad@gmail.com", password = encoder.encode("1234"), role = Role.ADMIN),
        User(UUID.randomUUID(), email = "email@gmail.com", password = encoder.encode("1234"), role = Role.USER),
        User(UUID.randomUUID(), email = "john@yahoo.com", password = encoder.encode("1234"), role = Role.USER)
        )

    fun save(user: User): Boolean {
        val updatedUser = user.copy(password = encoder.encode(user.password))

        return users.add(updatedUser)
    }

    fun findByEmail(email: String): User? =
        users
            .firstOrNull { it.email == email }

    fun findAll(): List<User> =
        users

    fun findByUUID(uuid: UUID): User? =
        users
            .firstOrNull{ it.id == uuid }

    fun deleteByUUID(uuid: UUID): Boolean {
        val foundUser = findByUUID(uuid)

        return foundUser?.let {
            users.remove(it)
        } ?: false
    }
}
