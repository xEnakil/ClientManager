package org.halflife.clientmanager.dto.response

data class ClientResponse(
    val email : String,
    val firstName : String,
    val lastName : String,
    val job: String?,
    val position: String?,
    val gender: String?
)
