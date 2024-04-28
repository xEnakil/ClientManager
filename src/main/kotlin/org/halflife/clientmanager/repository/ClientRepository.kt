package org.halflife.clientmanager.repository

import org.halflife.clientmanager.model.Client
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ClientRepository {

    private val clients = listOf(
        Client(id = UUID.randomUUID(), firstName = "Jane", lastName = "Doe", email = "jane.doe@gmail.com"),
        Client(id = UUID.randomUUID(), firstName = "Max", lastName = "Verstappen", email = "max.verstappen@yahoo.com")
    )

    fun findAll(): List<Client> = clients;
}