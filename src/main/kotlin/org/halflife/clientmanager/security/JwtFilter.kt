package org.halflife.clientmanager.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter  (
    private val userDetailsService: CustomUserDetailsService,
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader: String? = request.getHeader("Authorization")

        authHeader?.let {
            if (it.containsBearerToken()) {
                val jwtToken = it.getToken()
                processToken(jwtToken, request, response, filterChain)
            } else {
                filterChain.doFilter(request, response)
            }
        } ?: filterChain.doFilter(request, response)
    }

    private fun updateContext(userDetails: UserDetails, request: HttpServletRequest) {
        val token = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        token.details = WebAuthenticationDetailsSource().buildDetails(request)

        SecurityContextHolder.getContext().authentication = token
    }

    private fun String?.containsBearerToken(): Boolean =
        this != null && this.startsWith("Bearer ")

    private fun String.getToken(): String =
        this.substringAfter("Bearer ")

    private fun processToken(
        token: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        jwtService.extractEmail(token)?.let { email ->
            logger.info("Email extracted: $email")
            if (SecurityContextHolder.getContext().authentication == null) {
                userDetailsService.loadUserByUsername(email).let { userDetails ->
                    logger.info("User details loaded: $userDetails")
                    if (jwtService.validateJwtToken(token)) {
                        logger.info("JWT is valid, updating security context.")
                        updateContext(userDetails, request)
                    } else {
                        logger.warn("Invalid JWT token.")
                    }
                }
            }
        } ?: logger.warn("No email could be extracted from JWT token.")
        filterChain.doFilter(request, response)
    }
}