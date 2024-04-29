package org.halflife.clientmanager.mapper

import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.dto.response.FullClientResponse
import org.halflife.clientmanager.model.Client

interface ClientMapper {
    fun toResponse(client: Client) : ClientResponse
    fun toModel(clientRequest: ClientRequest) : Client
    fun toFullResponse(client: Client): FullClientResponse
}