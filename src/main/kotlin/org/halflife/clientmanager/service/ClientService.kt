package org.halflife.clientmanager.service

import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.repository.ClientRepository
import org.springframework.stereotype.Service

@Service
class ClientService (
    private val clientRepository: ClientRepository
) {

    fun findAll(): List<Client> = clientRepository.findAll()
}