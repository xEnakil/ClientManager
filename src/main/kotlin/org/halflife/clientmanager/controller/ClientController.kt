package org.halflife.clientmanager.controller

import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.service.ClientService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/clients")
class ClientController(
    private val clientService: ClientService
) {

    @GetMapping("/all")
    fun getClients() : List<ClientResponse> = clientService.findAll()
        .map { it.toResponse() }

    private fun Client.toResponse() : ClientResponse =
        ClientResponse(
            id = this.id,
            firstName = this.firstName,
            lastName = this.lastName,
            email = this.email
        )
}