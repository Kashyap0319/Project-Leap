package com.projectleap.tracker

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import javax.crypto.SecretKey
import java.time.Instant
import java.util.Date

class JwtService(secret: String, private val issuer: String = "api-monitoring") {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun sign(subject: String, ttlSeconds: Long = 3600): String =
        Jwts.builder()
            .subject(subject)
            .issuer(issuer)
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(ttlSeconds)))
            .signWith(key)
            .compact()

    fun parse(token: String): String =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject
}
