package org.halflife.clientmanager.repository

import org.halflife.clientmanager.model.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ClientRepository : JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {
    fun findByEmail(email: String): Client?
}
