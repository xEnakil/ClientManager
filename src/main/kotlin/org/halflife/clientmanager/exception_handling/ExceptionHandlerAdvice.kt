package org.halflife.clientmanager.exception_handling

import org.halflife.clientmanager.dto.response.ErrorResponse
import org.halflife.clientmanager.exception.DeleteAdminException
import org.halflife.clientmanager.exception.EmailAlreadyInUseException
import org.halflife.clientmanager.exception.InvalidCredentialsException
import org.halflife.clientmanager.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandlerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.allErrors.map { error ->
            val fieldName = (error as FieldError).field
            "$fieldName: ${error.defaultMessage}"
        }.joinToString(", ")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.name,
                message = "Validation error: $errors"
            ))
    }

    @ExceptionHandler(EmailAlreadyInUseException::class)
    fun handleEmailAlreadyInUseException(e: EmailAlreadyInUseException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.name,
                message = e.message ?: "Email is already in use"),
            )
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(e: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = HttpStatus.UNAUTHORIZED.name,
                message = e.message ?: "Invalid credentials"),
            )
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.
        status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = HttpStatus.NOT_FOUND.name,
                message = ex.message ?: "User not found"),
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val message = when {
            ex.message?.contains("Invalid UUID string") == true -> "Invalid UUID format provided."
            else -> ex.message ?: "Invalid argument provided."
        }

        return ResponseEntity.
        status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.name,
                message = message),
            )
    }

    @ExceptionHandler(DeleteAdminException::class)
    fun handleDeleteAdminException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {

        return ResponseEntity.
        status(HttpStatus.I_AM_A_TEAPOT)
            .body(ErrorResponse(
                status = HttpStatus.I_AM_A_TEAPOT.value(),
                error = HttpStatus.I_AM_A_TEAPOT.name,
                message = ex.message),
            )
    }


    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = HttpStatus.INTERNAL_SERVER_ERROR.name,
                message = "An unexpected error occurred.")
            )
    }
}