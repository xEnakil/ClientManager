package org.halflife.clientmanager.controller

import org.halflife.clientmanager.config.TestSecurityConfig
import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.dto.response.FullClientResponse
import org.halflife.clientmanager.mapper.ClientMapper
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.repository.ClientRepository
import org.halflife.clientmanager.security.CustomUserDetailsService
import org.halflife.clientmanager.security.JwtService
import org.halflife.clientmanager.service.AdminService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.nullable
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [AdminController::class])
@Import(TestSecurityConfig::class)
class AdminControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @MockBean
    private lateinit var jwtService: JwtService

    @MockBean
    private lateinit var adminService: AdminService

    @MockBean
    private lateinit var clientRepository: ClientRepository

    @MockBean
    private lateinit var clientMapper: ClientMapper

    @Test
    @WithMockUser(username="admin", roles=["ADMIN"])
    fun `add client should create client and return client response`() {
        val clientRequest = ClientRequest("email@example.com", "password123", "John", "Doe", null, null, null)
        val client = Client(UUID.randomUUID(), "email@example.com", "password123", "John", "Doe", Role.USER)
        val clientResponse = ClientResponse("email@example.com", "John", "Doe", null, null, null)

        `when`(adminService.addClient(clientRequest)).thenReturn(client)
        `when`(clientMapper.toResponse(client)).thenReturn(clientResponse)

        mockMvc.post("/admin/clients") {
            contentType = MediaType.APPLICATION_JSON
            content = "{\"email\":\"email@example.com\",\"password\":\"password123\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("email@example.com") }
            jsonPath("$.firstName") { value("John") }
        }
    }

    @Test
    @WithMockUser(username="admin", roles=["ADMIN"])
    fun `get client by id should return client response`() {
        val id = UUID.randomUUID().toString()
        val client = Client(UUID.fromString(id), "email@example.com", "password123", "John", "Doe", Role.ADMIN)
        val clientResponse = ClientResponse("email@example.com", "John", "Doe", null, null, null)

        `when`(adminService.getClientById(id)).thenReturn(client)
        `when`(clientMapper.toResponse(client)).thenReturn(clientResponse)

        mockMvc.get("/admin/clients/$id") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("email@example.com") }
        }
    }

    @Test
    @WithMockUser(username="admin", roles=["ADMIN"])
    fun `get clients returns paginated client responses`() {
        val page = 0
        val size = 10
        val client = Client(UUID.randomUUID(), "test@example.com", "securePass", "Test", "User", Role.ADMIN, "Developer", "Senior Dev", "male")
        val fullClientResponse = FullClientResponse("test@example.com", "test@example.com", "Test", "User", "Developer", "Senior Dev", "male",
            Role.USER.toString()
        )
        val clientList = listOf(client)
        val pageable = PageRequest.of(page, size)
        val clientPage = PageImpl(clientList, pageable, clientList.size.toLong())

        `when`(adminService.getClients(pageable)).thenReturn(clientPage)
        `when`(clientMapper.toFullResponse(client)).thenReturn(fullClientResponse)

        mockMvc.get("/admin/clients?page=$page&size=$size") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].email") { value("test@example.com") }
            jsonPath("$.content[0].firstName") { value("Test") }
            jsonPath("$.content[0].lastName") { value("User") }
            jsonPath("$.content[0].job") { value("Developer") }
            jsonPath("$.content[0].position") { value("Senior Dev") }
            jsonPath("$.content[0].gender") { value("male") }
            jsonPath("$.totalElements") { value(1) }
            jsonPath("$.totalPages") { value(1) }
            jsonPath("$.size") { value(size) }
            jsonPath("$.number") { value(page) }
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `delete client removes client and returns success message`() {
        val id = UUID.randomUUID()

        mockMvc.delete("/admin/clients/$id") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.message") { value("Client with $id has been deleted") }
        }

        val deletedClient = clientRepository.findById(id)
        assertTrue(deletedClient.isEmpty, "Client with ID $id should be deleted from the database")
    }

    @Test
    @WithMockUser(username="admin", roles=["ADMIN"])
    fun `search returns clients based on query`() {
        val searchQuery = "John"
        val clients = listOf(
            Client(UUID.randomUUID(), "john@example.com", "password", "John", "Doe", Role.ADMIN, "Developer", "Lead", "male"),
            Client(UUID.randomUUID(), "johnny@example.com", "password", "Johnny", "Dane", Role.ADMIN, "Manager", "Senior", "male")
        )
        val clientResponses = clients.map { client ->
            ClientResponse(client.email, client.firstName, client.lastName, client.job, client.position, client.gender)
        }

        `when`(adminService.search(searchQuery)).thenReturn(clients)
        `when`(clientMapper.toResponse(any(Client::class.java) ?: Client(UUID.randomUUID(), "", "", "", "", Role.USER))).thenAnswer { invocation ->
            val client = invocation.getArgument(0) as Client
            ClientResponse(client.email, client.firstName, client.lastName, client.job, client.position, client.gender)
        }
        mockMvc.get("/admin/search") {
            param("search", searchQuery)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(clientResponses.size) }
            jsonPath("$[0].email") { value(clientResponses[0].email) }
        }
    }
}