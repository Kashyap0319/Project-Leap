package com.projectleap.collector.service

import com.projectleap.collector.model.UserDocument
import com.projectleap.collector.security.JwtService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class AuthService(
    @Qualifier("metaTemplate") private val metaTemplate: MongoTemplate,
    private val jwtService: JwtService,
    private val passwordEncoder: BCryptPasswordEncoder
) {
    data class AuthResponse(val token: String)

    @Transactional("metaTxManager")
    fun signup(username: String, email: String, password: String): AuthResponse {
        if (username.isBlank() || email.isBlank() || password.isBlank()) throw IllegalArgumentException("Username/email/password required")
        val hash = passwordEncoder.encode(password)
        try {
            metaTemplate.insert(UserDocument(username = username, email = email, passwordHash = hash))
        } catch (ex: DuplicateKeyException) {
            throw IllegalArgumentException("User already exists")
        }
        val token = jwtService.sign(email)
        return AuthResponse(token)
    }

    fun login(email: String, password: String): AuthResponse {
        val user = metaTemplate.findOne(Query(Criteria.where("email").`is`(email)), UserDocument::class.java)
            ?: throw IllegalArgumentException("Invalid credentials")
        if (!passwordEncoder.matches(password, user.passwordHash)) throw IllegalArgumentException("Invalid credentials")
        return AuthResponse(jwtService.sign(email))
    }
}
