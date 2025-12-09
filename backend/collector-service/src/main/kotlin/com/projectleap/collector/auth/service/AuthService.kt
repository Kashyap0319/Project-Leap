package com.projectleap.collector.auth.service

import com.projectleap.collector.auth.repository.UserRepository
import com.projectleap.collector.dto.LoginRequest
import com.projectleap.collector.dto.SignupRequest
import com.projectleap.collector.model.Role
import com.projectleap.collector.model.User
import com.projectleap.collector.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    fun signup(request: SignupRequest): String {
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username already exists")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        val user = User(
            _username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            role = Role.USER
        )
        userRepository.save(user)
        return "User registered successfully"
    }

    fun login(request: LoginRequest): String {
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { IllegalArgumentException("Invalid username or password") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid username or password")
        }

        return jwtService.generateToken(user)
    }
}
