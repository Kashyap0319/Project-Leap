package com.projectleap.service

import com.projectleap.model.User
import com.projectleap.repository.metadata.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    fun createUser(username: String, password: String, email: String, roles: Set<String> = setOf("USER")): User {
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username already exists")
        }
        
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email already exists")
        }
        
        val user = User(
            username = username,
            password = passwordEncoder.encode(password),
            email = email,
            roles = roles
        )
        
        return userRepository.save(user)
    }
    
    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username).orElse(null)
    }
    
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
