package org.halflife.clientmanager.controller

import org.halflife.clientmanager.dto.request.LoginRequest
import org.halflife.clientmanager.dto.request.RefreshTokenRequest
import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.dto.response.LoginResponse
import org.halflife.clientmanager.dto.response.TokenResponse
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.service.AuthenticationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationService: AuthenticationService
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse =
        authenticationService.login(loginRequest)

    @PostMapping("/register")
    fun register(@RequestBody clientRequest: ClientRequest): ClientResponse =
        authenticationService.register(
            client = clientRequest.toModel()
        )?.toResponse()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create user")

    @PostMapping("/refresh-token")
    fun refreshToken(
        @RequestBody tokenRequest: RefreshTokenRequest
    ): TokenResponse =
        authenticationService.refreshAccessToken(tokenRequest.token)
            ?.mapToTokenResponse()
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh token")

    private fun String.mapToTokenResponse(): TokenResponse =
        TokenResponse(
            token = this
        )

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

    private fun Client.toResponse(): ClientResponse =
        ClientResponse(
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            gender = this.gender,
            job = this.job,
            position = this.position
        )
}