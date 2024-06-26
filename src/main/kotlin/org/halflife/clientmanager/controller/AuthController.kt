package org.halflife.clientmanager.controller

import jakarta.validation.Valid
import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.dto.request.LoginRequest
import org.halflife.clientmanager.dto.request.RefreshTokenRequest
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.dto.response.LoginResponse
import org.halflife.clientmanager.dto.response.TokenResponse
import org.halflife.clientmanager.mapper.ClientMapper
import org.halflife.clientmanager.service.AuthenticationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val clientMapper: ClientMapper
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> =
        ResponseEntity.ok().body(authenticationService.login(loginRequest))

    @PostMapping("/register")
    fun register(@Valid @RequestBody clientRequest: ClientRequest): ResponseEntity<ClientResponse> {
        val client = clientMapper.toModel(clientRequest)
        val registeringClient = authenticationService.register(client)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to register")
        val clientResponse = clientMapper.toResponse(registeringClient)
    return ResponseEntity.ok().body(clientResponse)
    }

    @PostMapping("/refresh-token")
    fun refreshToken(
        @RequestBody tokenRequest: RefreshTokenRequest
    ): TokenResponse =
        authenticationService.refreshAccessToken(tokenRequest.token)
            ?.mapToTokenResponse()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to refresh token")

    private fun String.mapToTokenResponse(): TokenResponse =
        TokenResponse(
            token = this
        )
}