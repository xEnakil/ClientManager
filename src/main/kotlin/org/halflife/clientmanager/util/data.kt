package org.halflife.clientmanager.util

import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.repository.ClientRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer(private val clientRepository: ClientRepository,
                      private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val adminEmail = "admin@example.com"

        if (clientRepository.findByEmail(adminEmail) == null) {
            val admin = Client(
                email = adminEmail,
                password = passwordEncoder.encode("1234"),
                firstName = "Admin",
                lastName = "User",
                role = Role.ADMIN
            )
            clientRepository.save(admin)
        }
    }
}