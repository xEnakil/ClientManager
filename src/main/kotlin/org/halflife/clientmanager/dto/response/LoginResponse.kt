package org.halflife.clientmanager.dto.response

data class LoginResponse (
    val accessToken: String,
    val refreshToken: String,
)
