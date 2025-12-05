package com.projectleap.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {
    
    @Value("\${jwt.secret}")
    private lateinit var secret: String
    
    @Value("\${jwt.expiration}")
    private var expiration: Long = 0
    
    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(secret.toByteArray())
    }
    
    fun generateToken(userDetails: UserDetails): String {
        val claims = HashMap<String, Any>()
        return createToken(claims, userDetails.username)
    }
    
    private fun createToken(claims: Map<String, Any>, subject: String): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact()
    }
    
    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }
    
    fun extractExpiration(token: String): Date {
        return extractAllClaims(token).expiration
    }
    
    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }
    
    fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }
    
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }
}
