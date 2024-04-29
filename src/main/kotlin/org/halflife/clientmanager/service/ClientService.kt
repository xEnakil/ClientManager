package org.halflife.clientmanager.service

import org.halflife.clientmanager.dto.request.ClientUpdateRequest
import org.halflife.clientmanager.exception.UserNotFoundException
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.repository.ClientRepository
import org.springframework.stereotype.Service
import java.nio.file.AccessDeniedException

@Service
class ClientService(
    private val clientRepository: ClientRepository,
) {

    fun getClientDetails(email: String): Client? {
        return clientRepository.findByEmail(email)
    }

    fun updateClientDetails(email: String, updatedClient: ClientUpdateRequest): Client {
        val client = getClientDetails(email)
            ?: throw UserNotFoundException(email)
        if (email == client.email) {
            updatedClient.firstName?.let { client.firstName = it }
            updatedClient.lastName?.let { client.lastName = it }
            updatedClient.gender?.let { client.gender = it.toString() }
            updatedClient.job?.let { client.job = it }
            updatedClient.position?.let { client.position = it }

            return clientRepository.save(client)
        } else {
            throw AccessDeniedException("You can only update your own information.")
        }
    }
}