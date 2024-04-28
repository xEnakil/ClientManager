package org.halflife.clientmanager.service

import org.halflife.clientmanager.dto.request.LoginRequest
import org.halflife.clientmanager.dto.response.LoginResponse
import org.halflife.clientmanager.model.User
import org.halflife.clientmanager.repository.RefreshTokenRepository
import org.halflife.clientmanager.repository.UserRepository
import org.halflife.clientmanager.security.CustomUserDetailsService
import org.halflife.clientmanager.security.JwtProperties
import org.halflife.clientmanager.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository
) {
    fun login(loginRequest: LoginRequest): LoginResponse {
        authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.email,
                loginRequest.password
            )
        )

        val user = userDetailsService.loadUserByUsername(loginRequest.email)
        val accessToken = generateAccessToken(user)
        val refreshToken = generateRefreshToken(user)

        refreshTokenRepository.save(refreshToken, user)


        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun register(user: User): User? {
        val found = userRepository.findByEmail(user.email)

        return if (found == null) {
            userRepository.save(user)
            user
        } else null
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

    private fun generateAccessToken(user: UserDetails) = jwtService.generateToken(
        userDetails = user,
        expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration),
    )

    private fun generateRefreshToken(user: UserDetails) = jwtService.generateToken(
        userDetails = user,
        expirationDate = Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration),
    )
}
