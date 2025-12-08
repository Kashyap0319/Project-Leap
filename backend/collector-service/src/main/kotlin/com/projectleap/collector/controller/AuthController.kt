package com.projectleap.collector.controller

import com.projectleap.collector.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    data class SignupRequest(val username: String, val email: String, val password: String)
    data class LoginRequest(val email: String, val password: String)

    @PostMapping("/signup")
    fun signup(@RequestBody req: SignupRequest): ResponseEntity<Any> {
        println("Signup attempt: username=${req.username}, email=${req.email}")
        return try {
            ResponseEntity.ok(authService.signup(req.username, req.email, req.password))
        } catch (ex: IllegalArgumentException) {
            println("Signup error: ${ex.message}")
            ResponseEntity.status(400).body(mapOf("error" to ex.message))
        } catch (ex: Exception) {
            println("Signup unexpected error: ${ex.message}")
            ResponseEntity.status(500).body(mapOf("error" to "Internal server error"))
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<AuthService.AuthResponse> =
        ResponseEntity.ok(authService.login(req.email, req.password))
}
