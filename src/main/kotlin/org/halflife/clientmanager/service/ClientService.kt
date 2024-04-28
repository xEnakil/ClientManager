package org.halflife.clientmanager.service

import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.repository.ClientRepository
import org.halflife.clientmanager.security.JwtService
import org.springframework.stereotype.Service

@Service
class ClientService(
    private val clientRepository: ClientRepository,
    private val jwtService: JwtService
) {

    fun getClientDetails(email: String): Client? {
        return clientRepository.findByEmail(email)
    }
}