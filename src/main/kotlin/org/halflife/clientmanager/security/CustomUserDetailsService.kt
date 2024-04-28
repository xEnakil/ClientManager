package org.halflife.clientmanager.security

import org.halflife.clientmanager.repository.ClientRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

typealias ApplicationUser = org.halflife.clientmanager.model.Client

@Service
class CustomUserDetailsService(
    private val clientRepository: ClientRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        clientRepository.findByEmail(username)
            ?.mapToUserDetails()
            ?: throw UsernameNotFoundException("User not found")

    private fun ApplicationUser.mapToUserDetails(): UserDetails =
        User.builder()
            .username(this.email)
            .password(this.password)
            .roles(this.role.name)
            .build()
}