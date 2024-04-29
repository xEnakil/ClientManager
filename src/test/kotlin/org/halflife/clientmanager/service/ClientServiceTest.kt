package org.halflife.clientmanager.service

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.halflife.clientmanager.dto.request.ClientUpdateRequest
import org.halflife.clientmanager.exception.UserNotFoundException
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.repository.ClientRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ClientServiceTest {
    private val clientRepository = mockk<ClientRepository>()
    private lateinit var clientService: ClientService

    @BeforeEach
    fun setup() {
        clientService = ClientService(clientRepository)
    }

    @Test
    fun `getClientDetails should return client when email is found`() {
        val email = "user@example.com"
        val client = Client(UUID.randomUUID(), email, "password", "John", "Doe", Role.USER, "Developer", "Senior", "male")

        every { clientRepository.findByEmail(email) } returns client

        val result = clientService.getClientDetails(email)

        assertNotNull(result)
        assertEquals(email, result?.email)
        verify(exactly = 1) { clientRepository.findByEmail(email) }
    }

    @Test
    fun `getClientDetails should return null when email is not found`() {
        val email = "nonexistent@example.com"

        every { clientRepository.findByEmail(email) } returns null

        val result = clientService.getClientDetails(email)

        assertNull(result)
    }

    @Test
    fun `updateClientDetails should update and return client when details are valid`() {
        val email = "user@example.com"
        val existingClient = Client(UUID.randomUUID(), email, "password", "John", "Doe", Role.USER, "Developer", "Senior", "male")
        val updatedDetails = ClientUpdateRequest("Johnny", "DoeNew", "female", "Engineer", "Lead")

        every { clientRepository.findByEmail(email) } returns existingClient
        every { clientRepository.save(any()) } answers { firstArg() }

        val result = clientService.updateClientDetails(email, updatedDetails)

        assertNotNull(result)
        assertEquals("Johnny", result.firstName)
        assertEquals("DoeNew", result.lastName)
        assertEquals("female", result.gender)
        assertEquals("Engineer", result.job)
        assertEquals("Lead", result.position)
        verify { clientRepository.save(existingClient) }
    }

    @Test
    fun `updateClientDetails should throw UserNotFoundException when client does not exist`() {
        val email = "nonexistent@example.com"
        val updatedDetails = ClientUpdateRequest("Johnny")

        every { clientRepository.findByEmail(email) } returns null

        val exception = assertThrows<UserNotFoundException> {
            clientService.updateClientDetails(email, updatedDetails)
        }

        assertEquals("Client with ID $email not found.", exception.message)
    }
}
