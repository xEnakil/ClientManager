package org.halflife.clientmanager.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ClientRequest(

    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email cannot be empty")
    val email : String,

    @field:NotBlank(message = "Password cannot be empty")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    val password : String,

    @field:NotBlank(message = "First name cannot be empty")
    val firstName : String,

    @field:NotBlank(message = "Last name cannot be empty")
    val lastName : String,

    val gender: String?,
    val job: String?,
    val position: String?,
)
