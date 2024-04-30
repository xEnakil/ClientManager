package org.halflife.clientmanager.dto.request

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(

    @field:NotBlank(message = "Token cannot be blank")
    val token: String,
)
