package org.halflife.clientmanager.service

import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.exception.DeleteAdminException
import org.halflife.clientmanager.exception.EmailAlreadyInUseException
import org.halflife.clientmanager.exception.UserNotFoundException
import org.halflife.clientmanager.mapper.ClientMapper
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.repository.ClientRepository
import org.halflife.clientmanager.specifications.ClientSpecifications
import org.halflife.clientmanager.util.GenderDetection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdminService(
    private val clientRepository: ClientRepository,
    private val encoder: PasswordEncoder,
    private val genderDetection: GenderDetection,
    private val clientSpecifications: ClientSpecifications,
    private val clientMapper: ClientMapper,
) {

    fun addClient(clientRequest: ClientRequest): Client? {
        clientRepository.findByEmail(clientRequest.email)?.let {
            throw EmailAlreadyInUseException(clientRequest.email)
        }

        val encodedPassword = encoder.encode(clientRequest.password)

        val client: Client = clientMapper.toModel(clientRequest).apply {
            this.password = encodedPassword
            this.gender = detectGender(this.firstName)
        }

        return clientRepository.save(client)
    }

    fun getClientById(id: String): Client? {
        val clientUuid = UUID.fromString(id)
        val client = clientRepository.findById(clientUuid).orElseThrow {
            UserNotFoundException("Client with ID $id not found.")
        }

        return client
    }

    fun getClients(pageable: Pageable): Page<Client> {
        return clientRepository.findAll(pageable)
    }

    fun removeClient(id: UUID) {
        val auth = SecurityContextHolder.getContext().authentication
        val currentUserEmail = (auth.principal as UserDetails).username

        val client = clientRepository.findById(id).orElse(null)

        if (client != null && client.email == currentUserEmail) {
            throw DeleteAdminException("Are you serious right know ? You going to really try to delete yourself ? 0_0")
        }
    }

    fun search(string: String) : List<Client> {
        val spec: Specification<Client> = clientSpecifications.search(string)
        return clientRepository.findAll(spec)
    }


    private fun detectGender(firstName: String): String {
        var gender = "Undetected"
        val firstNameResponse = genderDetection.getGender(firstName)
        if ((firstNameResponse?.probability ?: 0.0) >= 0.8) {
            gender = firstNameResponse?.gender ?: "Undetected"
        }

        return gender
    }
}