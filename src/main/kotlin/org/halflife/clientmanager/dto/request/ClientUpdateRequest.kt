package org.halflife.clientmanager.dto.request

import jakarta.validation.constraints.NotBlank

data class ClientUpdateRequest(

    @field:NotBlank(message = "First name cannot be empty")
    val firstName: String? = null,

    @field:NotBlank(message = "Last name cannot be empty")
    val lastName: String? = null,
    val gender: String? = null,
    val job: String? = null,
    val position: String? = null
)
