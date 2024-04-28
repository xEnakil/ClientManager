package org.halflife.clientmanager.service

import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.repository.ClientRepository
import org.halflife.clientmanager.specifications.ClientSpecifications
import org.halflife.clientmanager.util.GenderDetection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdminService(
    private val clientRepository: ClientRepository,
    private val encoder: PasswordEncoder,
    private val genderDetection: GenderDetection,
    private val clientSpecifications: ClientSpecifications
) {

    fun addClient(clientRequest: ClientRequest): Client? {
        clientRepository.findByEmail(clientRequest.email)?.let {
            throw Exception("Client with email ${clientRequest.email} already exists.")
        }

        val encodedPassword = encoder.encode(clientRequest.password)

        val client: Client = clientRequest.toModel()
        client.password = encodedPassword
        client.gender = detectGender(client.firstName)
        return clientRepository.save(client)
    }

    fun getClientById(id: String): Client? {
        val clientUuid = UUID.fromString(id)
        val client = clientRepository.findById(clientUuid).orElseThrow {
            NoSuchElementException("Client with ID $id not found.")
        }

        return client
    }

    fun getClients(pageable: Pageable): Page<Client> {
        return clientRepository.findAll(pageable)
    }

    fun removeClient(id: UUID) {
        clientRepository.deleteById(id)
    }

    fun search(string: String) : List<Client> {
        val spec: Specification<Client> = clientSpecifications.search(string)
        return clientRepository.findAll(spec)
    }

    private fun ClientRequest.toModel(): Client =
        Client(
            email = this.email,
            password = this.password,
            firstName = this.firstName,
            lastName = this.lastName,
            gender = this.gender,
            role = Role.USER,
            job = this.job,
            position = this.position
        )

    private fun detectGender(firstName: String): String {
        var gender = "Undetected"
        val firstNameResponse = genderDetection.getGender(firstName)
        if ((firstNameResponse?.probability ?: 0.0) >= 0.8) {
            gender = firstNameResponse?.gender ?: "Undetected"
        }

        return gender
    }
}