package org.halflife.clientmanager.dto.response

data class GenderResponse(
    val name: String,
    val gender: String?,
    val probability: Double,
    val count: Int,
)
