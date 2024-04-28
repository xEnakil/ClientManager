package org.halflife.clientmanager.model

import java.util.UUID

data class User(
    val id : UUID,
    val email : String,
    val password : String,
    val role: Role
)

enum class Role {
    USER, ADMIN
}
