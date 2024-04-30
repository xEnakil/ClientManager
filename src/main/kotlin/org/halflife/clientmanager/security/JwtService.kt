package org.halflife.clientmanager.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.SignatureException
import java.util.Date

@Service
class JwtService(
    jwtProperties: JwtProperties
) {

    private val secretKey = Keys.hmacShaKeyFor(
        jwtProperties.key.toByteArray(),
    )

    fun generateToken(
        userDetails: UserDetails,
        expirationDate: Date,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String =
        Jwts.builder()
            .claims()
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expirationDate)
            .add(additionalClaims)
            .and()
            .signWith(secretKey)
            .compact()

    fun extractEmail(token: String): String? =
        getAllClaims(token.trim()).subject

    fun isExpired(token: String): Boolean =
        getAllClaims(token)
            .expiration
            .before(Date(System.currentTimeMillis()))

    fun validateJwtToken(authToken: String): Boolean{
        try{
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(authToken)
            return true;
        }catch (e: SignatureException){
            val msg = e.message
            println("Invalid JWT signature: $msg" )
        } catch (e: MalformedJwtException){
            val msg = e.message
            println("Invalid JWT token: $msg")

        }catch (e: IllegalArgumentException){
            val msg = e.message
            println("JWT claims string is empty: $msg")
        }
        return false;
    }

    private fun getAllClaims(token: String): Claims {
        var parser = Jwts.parser()
            .verifyWith(secretKey)
            .build()

        return parser
            .parseSignedClaims(token)
            .payload
    }

    //Old Validation
    fun isValid(token: String, userDetails: UserDetails): Boolean {
        val email = extractEmail(token)

        return userDetails.username == email && !isExpired(token)
    }
}