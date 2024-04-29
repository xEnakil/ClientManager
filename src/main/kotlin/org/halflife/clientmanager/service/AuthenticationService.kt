package org.halflife.clientmanager.service

import org.halflife.clientmanager.dto.request.LoginRequest
import org.halflife.clientmanager.dto.response.LoginResponse
import org.halflife.clientmanager.exception.EmailAlreadyInUseException
import org.halflife.clientmanager.exception.InvalidCredentialsException
import org.halflife.clientmanager.exception.UserNotFoundException
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.repository.RefreshTokenRepository
import org.halflife.clientmanager.repository.ClientRepository
import org.halflife.clientmanager.security.CustomUserDetailsService
import org.halflife.clientmanager.security.JwtProperties
import org.halflife.clientmanager.security.JwtService
import org.halflife.clientmanager.util.GenderDetection
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.Date
import kotlin.math.log

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val clientRepository: ClientRepository,
    private val encoder: PasswordEncoder,
    private val genderDetection: GenderDetection
) {
    fun login(loginRequest: LoginRequest): LoginResponse {
        try {
            authManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.email,
                    loginRequest.password
                )
            )
        } catch (ex: AuthenticationException) {
            throw InvalidCredentialsException()
        }

        val user = userDetailsService.loadUserByUsername(loginRequest.email)

        val accessToken = generateAccessToken(user)
        val refreshToken = generateRefreshToken(user)

        refreshTokenRepository.save(refreshToken, user)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun register(client: Client): Client? {
        clientRepository.findByEmail(client.email)?.let {
            throw EmailAlreadyInUseException(client.email)
        }

        if(client.gender.isNullOrEmpty()) {
            client.gender = detectGender(client.firstName)
        }

        client.password = encoder.encode(client.password)
        return clientRepository.save(client)
    }

    fun refreshAccessToken(token: String): String? {
        val email = jwtService.extractEmail(token)

        return email?.let { email ->
            val currentUserDetails = userDetailsService.loadUserByUsername(email)
            val refreshTokenUserDetails = refreshTokenRepository.findUserDetailsByToken(token)

            if (!jwtService.isExpired(token) && currentUserDetails.username == refreshTokenUserDetails?.username)
                generateAccessToken(currentUserDetails)
            else
                null
        }
    }

    private fun detectGender(firstName: String): String {
        var gender = "Undetected"
        val firstNameResponse = genderDetection.getGender(firstName)
        if ((firstNameResponse?.probability ?: 0.0) >= 0.8) {
            gender = firstNameResponse?.gender ?: "Undetected"
        }

        return gender
    }

    private fun generateAccessToken(user: UserDetails) = jwtService.generateToken(
        userDetails = user,
        expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration),
    )

    private fun generateRefreshToken(user: UserDetails) = jwtService.generateToken(
        userDetails = user,
        expirationDate = Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration),
    )
}
