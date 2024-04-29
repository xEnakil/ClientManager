package org.halflife.clientmanager.config

import org.halflife.clientmanager.security.CustomUserDetailsService
import org.halflife.clientmanager.security.JwtFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val authenticationProvider: AuthenticationProvider,
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtFilter: JwtFilter, customUserDetailsService: CustomUserDetailsService
    ): DefaultSecurityFilterChain =
        http
            .csrf { it.disable() }
//            .exceptionHandling {it.authenticationEntryPoint(customAuthenticationEntryPoint)}
            .authorizeHttpRequests {
                it
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/database/**").permitAll()
                    .requestMatchers("/admin/testing-exception").permitAll()
                    .anyRequest().fullyAuthenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .userDetailsService(customUserDetailsService)
            .headers { it.frameOptions{ it.disable() } }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}