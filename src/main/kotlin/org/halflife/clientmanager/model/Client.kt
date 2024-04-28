package org.halflife.clientmanager.model

import java.util.UUID

data class Client(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String
)
