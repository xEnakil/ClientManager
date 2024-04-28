package org.halflife.clientmanager.dto.request

data class ClientRequest(
    val email : String,
    val password : String,
    val firstName : String,
    val lastName : String,
    val gender: String?,
    val job: String?,
    val position: String?,
)
