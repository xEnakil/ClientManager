package org.halflife.clientmanager.dto.request

data class ClientUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val job: String? = null,
    val position: String? = null
)
