package org.halflife.clientmanager.dto.response

import java.util.UUID

data class UserResponse(
    val uuid: UUID,
    val email : String
)
