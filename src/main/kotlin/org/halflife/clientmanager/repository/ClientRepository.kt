package org.halflife.clientmanager.repository

import org.halflife.clientmanager.model.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ClientRepository : JpaRepository<Client, UUID> {
    fun findByEmail(email: String): Client?
}
