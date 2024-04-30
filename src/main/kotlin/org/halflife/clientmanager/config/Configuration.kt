package org.halflife.clientmanager.config

import org.halflife.clientmanager.repository.ClientRepository
import org.halflife.clientmanager.security.CustomUserDetailsService
import org.halflife.clientmanager.security.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class ServiceConfiguration {

    @Bean
    fun userDetailsService(clientRepository: ClientRepository): UserDetailsService =
        CustomUserDetailsService(clientRepository)

    @Bean
    fun encoder(): PasswordEncoder = BCryptPasswordEncoder();

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate();

    @Bean
    fun authenticationProvider(clientRepository: ClientRepository): AuthenticationProvider =
        DaoAuthenticationProvider()
            .also {
                it.setUserDetailsService(userDetailsService(clientRepository))
                it.setPasswordEncoder(encoder())
            }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager;
}