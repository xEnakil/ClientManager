package org.halflife.clientmanager.service

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.dto.response.GenderResponse
import org.halflife.clientmanager.exception.EmailAlreadyInUseException
import org.halflife.clientmanager.exception.UserNotFoundException
import org.halflife.clientmanager.mapper.ClientMapper
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.repository.ClientRepository
import org.halflife.clientmanager.specifications.ClientSpecifications
import org.halflife.clientmanager.util.GenderDetection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class AdminServiceTest {

    private val clientRepository = mockk<ClientRepository>(relaxed = true)
    private val encoder = mockk<PasswordEncoder>(relaxed = true)
    private val genderDetection = mockk<GenderDetection>(relaxed = true)
    private val clientMapper = mockk<ClientMapper>(relaxed = true)
    private val clientSpecifications = mockk<ClientSpecifications>(relaxed = true)
    private val adminService = AdminService(clientRepository, encoder, genderDetection, clientSpecifications, clientMapper)


    @Test
    fun `should add new client when email does not exist`() {
        val clientRequest = ClientRequest(
            email = "new@email.com",
            password = "Password123",
            firstName = "John",
            lastName = "Doe",
            gender = null,
            job = null,
            position = null
        )
        val expectedClient = Client(
            id = UUID.randomUUID(),
            email = clientRequest.email,
            password = "encodedPassword",
            firstName = clientRequest.firstName,
            lastName = clientRequest.lastName,
            role = Role.USER
        )

        every { clientRepository.findByEmail(clientRequest.email) } returns null
        every { encoder.encode(clientRequest.password) } returns "encodedPassword"
        every { clientMapper.toModel(clientRequest) } returns expectedClient
        every { genderDetection.getGender(clientRequest.firstName) } returns GenderResponse(clientRequest.firstName, "male", 0.9, 100)
        every { clientRepository.save(any()) } returns expectedClient

        val result = adminService.addClient(clientRequest)

        assertNotNull(result)
        assertEquals(expectedClient, result)
        verify(exactly = 1) { clientRepository.save(expectedClient) }
    }

    @Test
    fun `should throw EmailAlreadyInUseException if email already exists`() {
        val clientRequest = ClientRequest(
            email = "existing@email.com",
            password = "Password123",
            firstName = "John",
            lastName = "Doe",
            gender = null,
            job = null,
            position = null
        )
        every { clientRepository.findByEmail(clientRequest.email) } returns Client(
            UUID.randomUUID(),
            clientRequest.email,
            "someEncodedPassword",
            clientRequest.firstName,
            clientRequest.lastName,
            Role.USER
        )

        assertThrows<EmailAlreadyInUseException> {
            adminService.addClient(clientRequest)
        }
    }

    @Test
    fun `should return client when id exists`() {
        val validUuid = UUID.randomUUID().toString()
        val expectedClient = Client(
            id = UUID.fromString(validUuid),
            email = "test@example.com",
            password = "encryptedPassword",
            firstName = "Test",
            lastName = "User",
            role = Role.USER
        )
        every { clientRepository.findById(UUID.fromString(validUuid)) } returns Optional.of(expectedClient)

        val result = adminService.getClientById(validUuid)

        assertNotNull(result)
        assertEquals(expectedClient, result)
    }

    @Test
    fun `should throw UserNotFoundException when client id does not exist`() {
        val invalidUuid = UUID.randomUUID().toString()
        every { clientRepository.findById(UUID.fromString(invalidUuid)) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            adminService.getClientById(invalidUuid)
        }
    }

    @Test
    fun `should return paginated clients`() {
        val pageable = mockk<Pageable>(relaxed = true)
        val clients = listOf(
            Client(UUID.randomUUID(), "user1@example.com", "encryptedPassword1", "User", "One", Role.USER),
            Client(UUID.randomUUID(), "user2@example.com", "encryptedPassword2", "User", "Two", Role.USER)
        )
        val pageClients = mockk<Page<Client>>()

        every { pageClients.content } returns clients
        every { pageClients.totalElements } returns clients.size.toLong()
        every { pageClients.numberOfElements } returns clients.size
        every { clientRepository.findAll(pageable) } returns pageClients

        val result = adminService.getClients(pageable)

        assertNotNull(result)
        assertEquals(clients.size, result.numberOfElements)
        assertEquals(clients, result.content)
        verify(exactly = 1) { clientRepository.findAll(pageable) }
    }

    @Test
    fun `simple page creation test`() {
        val clients = listOf(
            Client(UUID.randomUUID(), "user1@example.com", "password1", "User", "One", Role.USER)
        )
        val pageable = Pageable.unpaged()
        val totalElements = 1L

        try {
            val page = PageImpl(clients, pageable, totalElements)
            assertNotNull(page)
        } catch (e: Exception) {
            fail("Failed to create PageImpl: ${e.message}")
        }
    }

    @Test
    fun `should throw UserNotFoundException if client does not exist`() {
        val nonExistentId = UUID.randomUUID()
        every { clientRepository.existsById(nonExistentId) } returns false

        val exception = assertThrows<UserNotFoundException> {
            adminService.removeClient(nonExistentId)
        }

        assertEquals("Client with ID $nonExistentId not found.", exception.message)
    }

    @Test
    fun `should remove client when client exists`() {
        val existingId = UUID.randomUUID()
        every { clientRepository.existsById(existingId) } returns true

        adminService.removeClient(existingId)

        verify(exactly = 1) { clientRepository.deleteById(existingId) }
    }

    @Test
    fun `should return clients based on search criteria`() {
        val searchString = "test"
        val spec = mockk<Specification<Client>>()
        val expectedClients = listOf(
            Client(UUID.randomUUID(), "test1@example.com", "password1", "Test", "User", Role.USER),
            Client(UUID.randomUUID(), "test2@example.com", "password2", "Test", "UserTwo", Role.USER)
        )

        every { clientSpecifications.search(searchString) } returns spec
        every { clientRepository.findAll(spec) } returns expectedClients

        val result = adminService.search(searchString)

        assertNotNull(result)
        assertEquals(expectedClients, result)
        verify(exactly = 1) { clientSpecifications.search(searchString) }
        verify(exactly = 1) { clientRepository.findAll(spec) }
    }
}