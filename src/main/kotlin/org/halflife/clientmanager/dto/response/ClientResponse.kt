package org.halflife.clientmanager.dto.response

import java.util.UUID

data class ClientResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String)
