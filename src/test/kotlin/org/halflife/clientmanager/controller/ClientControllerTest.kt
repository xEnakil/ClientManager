package org.halflife.clientmanager.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.halflife.clientmanager.config.TestSecurityConfig
import org.halflife.clientmanager.dto.request.ClientUpdateRequest
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.mapper.ClientMapper
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.security.CustomUserDetailsService
import org.halflife.clientmanager.security.JwtService
import org.halflife.clientmanager.service.ClientService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [ClientController::class])
@Import(TestSecurityConfig::class)
class ClientControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var clientService: ClientService

    @MockBean
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @MockBean
    private lateinit var jwtService: JwtService

    @MockBean
    private lateinit var clientMapper: ClientMapper

    @Test
    @WithMockUser(username = "test@example.com", roles = ["USER"])
    fun `get client details should return client response`() {
        val client = Client(UUID.randomUUID(), "test@example.com", "password", "John", "Doe", Role.USER)
        val clientResponse = ClientResponse("test@example.com", "John", "Doe", null, null, null)

        given(clientService.getClientDetails("test@example.com")).willReturn(client)
        given(clientMapper.toResponse(client)).willReturn(clientResponse)

        mockMvc.get("/client/details") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.email") { value("test@example.com") }
        }
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = ["USER"])
    fun `update client details should return client response`() {
        val updateRequest = ClientUpdateRequest(firstName = "John", lastName = "Doe");      val client = Client(UUID.randomUUID(), "test@example.com", "password", "John", "Doe", Role.USER)
        val clientResponse = ClientResponse("test@example.com", "Marx", "Doe", null, null, null)

        given(clientService.updateClientDetails("test@example.com", updateRequest)).willReturn(client)
        given(clientMapper.toResponse(client)).willReturn(clientResponse)

        mockMvc.patch("/client/update") {
            contentType = MediaType.APPLICATION_JSON
            content = jacksonObjectMapper().writeValueAsString(updateRequest)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.firstName") { value("Marx") }
        }
    }
}