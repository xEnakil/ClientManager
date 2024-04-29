package org.halflife.clientmanager.mapper.impl

import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.dto.response.FullClientResponse
import org.halflife.clientmanager.mapper.ClientMapper
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.springframework.stereotype.Component

@Component
class ClientMapperImpl : ClientMapper {
    override fun toResponse(client: Client): ClientResponse =
        ClientResponse(
            email = client.email,
            firstName = client.firstName,
            lastName = client.lastName,
            job = client.job,
            position = client.position,
            gender = client.gender,
        )

    override fun toModel(clientRequest: ClientRequest): Client =
        Client(
            email = clientRequest.email,
            password = clientRequest.password,
            firstName = clientRequest.firstName,
            lastName = clientRequest.lastName,
            gender = clientRequest.gender,
            role = Role.USER,
            job = clientRequest.job,
            position = clientRequest.position,
        )

    override fun toFullResponse(client: Client): FullClientResponse =
        FullClientResponse(
            id = client.id.toString(),
            email = client.email,
            firstName = client.firstName,
            lastName = client.lastName,
            job = client.job.toString(),
            position = client.position.toString(),
            role = client.role.toString(),
            gender = client.gender.toString()
        )
}