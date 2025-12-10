package com.projectleap.collector.auth.controller

import com.projectleap.collector.auth.service.AuthService
import com.projectleap.collector.dto.LoginRequest
import com.projectleap.collector.dto.SignupRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<Map<String, String>> {
        val token = authService.signup(request)
        return ResponseEntity.ok(mapOf("token" to token))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Map<String, String>> {
        val token = authService.login(request)
        return ResponseEntity.ok(mapOf("token" to token))
    }
}
