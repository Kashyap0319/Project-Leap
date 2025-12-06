package com.projectleap.collector.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader("Authorization") ?: return chain.doFilter(request, response)
        if (!header.startsWith("Bearer ")) return chain.doFilter(request, response)
        val token = header.removePrefix("Bearer ").trim()
        val subject = try { jwtService.parseSubject(token) } catch (_: Exception) { null }
        if (subject != null) {
            val auth = UsernamePasswordAuthenticationToken(subject, null, listOf(SimpleGrantedAuthority("ROLE_USER")))
            auth.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = auth
        }
        chain.doFilter(request, response)
    }
}
