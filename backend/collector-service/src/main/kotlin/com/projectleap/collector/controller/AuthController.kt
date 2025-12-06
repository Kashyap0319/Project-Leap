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
    data class AuthRequest(val username: String, val password: String)

    @PostMapping("/signup")
    fun signup(@RequestBody req: AuthRequest): ResponseEntity<AuthService.AuthResponse> =
        ResponseEntity.ok(authService.signup(req.username, req.password))

    @PostMapping("/login")
    fun login(@RequestBody req: AuthRequest): ResponseEntity<AuthService.AuthResponse> =
        ResponseEntity.ok(authService.login(req.username, req.password))
}
