package org.halflife.clientmanager.dto.response

data class FullClientResponse(
    val id : String,
    val email : String,
    val firstName : String,
    val lastName : String,
    val job: String,
    val position: String,
    val gender: String,
    val role: String,
)
