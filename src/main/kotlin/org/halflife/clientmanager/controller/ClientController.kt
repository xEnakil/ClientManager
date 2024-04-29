package org.halflife.clientmanager.controller

import org.halflife.clientmanager.dto.request.ClientUpdateRequest
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.mapper.ClientMapper
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.service.ClientService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/client")
class ClientController(
    private val clientService: ClientService,
    private val clientMapper: ClientMapper
) {

    @GetMapping("/details")
    fun getClientDetails(authentication: Authentication): ResponseEntity<ClientResponse> {
        val email = (authentication.principal as UserDetails).username
        val client = clientService.getClientDetails(email)
        return if (client != null) {
            ResponseEntity.ok().body(clientMapper.toResponse(client))
        } else
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @PatchMapping("/update")
    fun updateClientDetails(@RequestBody updateRequest: ClientUpdateRequest ,authentication: Authentication): ResponseEntity<ClientResponse> {
        val email = (authentication.principal as UserDetails).username
        return ResponseEntity.ok().body(clientMapper.toResponse(clientService.updateClientDetails(email, updateRequest)))
    }
}