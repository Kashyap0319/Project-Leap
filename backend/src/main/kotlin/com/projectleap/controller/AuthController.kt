package com.projectleap.controller

import com.projectleap.security.JwtTokenProvider
import com.projectleap.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.bind.annotation.*

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String
)

data class AuthResponse(
    val token: String,
    val username: String,
    val email: String
)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService
) {
    
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
        )
        
        val userDetails = userDetailsService.loadUserByUsername(loginRequest.username)
        val token = jwtTokenProvider.generateToken(userDetails)
        
        val user = userService.getUserByUsername(loginRequest.username)
            ?: throw IllegalStateException("User not found")
        
        return ResponseEntity.ok(
            AuthResponse(
                token = token,
                username = user.username,
                email = user.email
            )
        )
    }
    
    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<Map<String, String>> {
        userService.createUser(
            username = registerRequest.username,
            password = registerRequest.password,
            email = registerRequest.email
        )
        
        return ResponseEntity.ok(mapOf("message" to "User registered successfully"))
    }
}
