package com.projectleap.collector.service

import com.projectleap.collector.model.UserDocument
import com.projectleap.collector.security.JwtService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class AuthService(
    @Qualifier("metaTemplate") private val metaTemplate: MongoTemplate,
    private val jwtService: JwtService,
    private val passwordEncoder: BCryptPasswordEncoder
) {
    data class AuthResponse(val token: String)

    fun signup(username: String, password: String): AuthResponse {
        if (username.isBlank() || password.isBlank()) throw IllegalArgumentException("Username/password required")
        val hash = passwordEncoder.encode(password)
        try {
            metaTemplate.insert(UserDocument(username = username, passwordHash = hash))
        } catch (ex: DuplicateKeyException) {
            throw IllegalArgumentException("User already exists")
        }
        val token = jwtService.sign(username)
        return AuthResponse(token)
    }

    fun login(username: String, password: String): AuthResponse {
        val user = metaTemplate.findOne(Query(Criteria.where("username").`is`(username)), UserDocument::class.java)
            ?: throw IllegalArgumentException("Invalid credentials")
        if (!passwordEncoder.matches(password, user.passwordHash)) throw IllegalArgumentException("Invalid credentials")
        return AuthResponse(jwtService.sign(username))
    }
}
