package org.halflife.clientmanager.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.halflife.clientmanager.config.TestSecurityConfig
import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.dto.request.LoginRequest
import org.halflife.clientmanager.dto.request.RefreshTokenRequest
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.dto.response.LoginResponse
import org.halflife.clientmanager.dto.response.TokenResponse
import org.halflife.clientmanager.mapper.ClientMapper
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.repository.RefreshTokenRepository
import org.halflife.clientmanager.security.CustomUserDetailsService
import org.halflife.clientmanager.security.JwtFilter
import org.halflife.clientmanager.security.JwtService
import org.halflife.clientmanager.service.AuthenticationService
import org.halflife.clientmanager.service.ClientService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [AuthController::class])
@Import(TestSecurityConfig::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var authenticationService: AuthenticationService

    @MockBean
    private lateinit var authenticationManager: AuthenticationManager

    @MockBean
    private lateinit var userDetailsService: CustomUserDetailsService

    @MockBean
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @MockBean
    private lateinit var clientMapper: ClientMapper

    @MockBean
    private lateinit var jwtFilter: JwtFilter


    @Test
    @WithMockUser
    fun `login should authenticate user and return login response`() {
        val loginRequest = LoginRequest("test@example.com", "password")
        val userDetails = User("test@example.com", "password", listOf(SimpleGrantedAuthority("ROLE_USER")))
        val loginResponse = LoginResponse("access_token", "refresh_token")
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)

        `when`(authenticationManager.authenticate(any(Authentication::class.java))).thenReturn(authentication)
        `when`(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails)
        `when`(authenticationService.login(loginRequest)).thenReturn(loginResponse)

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = jacksonObjectMapper().writeValueAsString(loginRequest)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }  // Check that the content type of the response is correct
            jsonPath("$.access_token") { value("access_token") }
            jsonPath("$.refresh_token") { value("refresh_token") }
        }
    }

    @Test
    fun `register should create new user and return client response`() {
        val clientRequest = ClientRequest("new@example.com", "password", "New", "User", null, null, null)
        val client = Client(UUID.randomUUID(), "new@example.com", "password", "New", "User", Role.USER)
        val clientResponse = ClientResponse("new@example.com", "New", "User", null, null, null)

        `when`(clientMapper.toModel(clientRequest)).thenReturn(client)
        `when`(authenticationService.register(client)).thenReturn(client)
        `when`(clientMapper.toResponse(client)).thenReturn(clientResponse)

        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = jacksonObjectMapper().writeValueAsString(clientRequest)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.email") { value("new@example.com") }
        }
    }

    @Test
    fun `refresh token should update access token and return it`() {
        val tokenRequest = RefreshTokenRequest("old_refresh_token")
        val expectedResponse = TokenResponse("new_access_token")

        `when`(authenticationService.refreshAccessToken(tokenRequest.token)).thenReturn("new_access_token")

        mockMvc.post("/auth/refresh-token") {
            contentType = MediaType.APPLICATION_JSON
            content = jacksonObjectMapper().writeValueAsString(tokenRequest)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.token") { value("new_access_token") }
        }
    }
}