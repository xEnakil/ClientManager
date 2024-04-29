package org.halflife.clientmanager.service

import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.halflife.clientmanager.dto.request.LoginRequest
import org.halflife.clientmanager.dto.response.GenderResponse
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.repository.ClientRepository
import org.halflife.clientmanager.repository.RefreshTokenRepository
import org.halflife.clientmanager.security.CustomUserDetailsService
import org.halflife.clientmanager.security.JwtProperties
import org.halflife.clientmanager.security.JwtService
import org.halflife.clientmanager.util.GenderDetection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.Date
import java.util.UUID
import kotlin.time.Duration.Companion.microseconds

@ExtendWith(MockKExtension::class)
class AuthenticationServiceTest{

    private val authManager = mockk<AuthenticationManager>()
    private val userDetailsService = mockk<CustomUserDetailsService>()
    private val jwtService = mockk<JwtService>()
    private val jwtProperties = mockk<JwtProperties>(relaxed = true)  // Assuming we might just need simple properties access
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val encoder = mockk<PasswordEncoder>(relaxed = true)
    private val clientRepository = mockk<ClientRepository>(relaxed = true)
    private val genderDetection = mockk<GenderDetection>(relaxed = true)

    private lateinit var authenticationService: AuthenticationService

    @BeforeEach
    fun setup() {
        authenticationService = AuthenticationService(
            authManager,
            userDetailsService,
            jwtService,
            jwtProperties,
            refreshTokenRepository,
            clientRepository,
            encoder,
            genderDetection,
        )
    }

    @Test
    fun `register should save new client if email does not exist`() {
        val client = Client(UUID.randomUUID(), "newuser@example.com", "password123", "John", "Doe", Role.USER)
        val encodedPassword = "encodedPassword"

        every { clientRepository.findByEmail(client.email) } returns null
        every { encoder.encode(client.password) } returns encodedPassword
        every { genderDetection.getGender(client.firstName) } returns GenderResponse(client.firstName, "Male", 0.8, 2313)
        every { clientRepository.save(any()) } answers { firstArg() }

        val result = authenticationService.register(client)

        assertNotNull(result)
        assertEquals(encodedPassword, result?.password)
        assertEquals("Male", result?.gender)
        verify(exactly = 1) { clientRepository.save(any()) }
    }

    //Not working yet
    @Test
    fun `refreshAccessToken should return new access token if refresh token is valid`() {
        val email = "user@example.com"
        val token = "valid_refresh_token"
        val userDetails = mockk<UserDetails>()
        val newUserDetails = mockk<UserDetails>(relaxed = true)
        val newAccessToken = "new_access_token"
        val accessTokenExpiration = jwtProperties.accessTokenExpiration
        val date = accessTokenExpiration.toDate()



        every { jwtService.extractEmail(token) } returns email
        every { userDetailsService.loadUserByUsername(email) } returns userDetails
        every { refreshTokenRepository.findUserDetailsByToken(token) } returns newUserDetails
        every { jwtService.isExpired(token) } returns false
        every { jwtService.generateToken(userDetails, date) } returns newAccessToken

        val result = authenticationService.refreshAccessToken(token)

        assertNotNull(result)
        assertEquals(newAccessToken, result)
        verify { jwtService.generateToken(userDetails, date) }
    }

    private fun Long.toDate(): Date {
        return Date.from(Instant.ofEpochMilli(this))
    }

    @Test
    fun `refreshAccessToken should return null if refresh token is expired`() {
        val token = "expired_refresh_token"

        every { jwtService.extractEmail(token) } returns null

        val result = authenticationService.refreshAccessToken(token)

        assertNull(result)
    }
}