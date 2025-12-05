package com.projectleap.util

import com.projectleap.config.RateLimitConfig
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class RateLimiter(
    private val rateLimitConfig: RateLimitConfig
) {
    
    fun checkRateLimit(request: HttpServletRequest, userId: String) {
        val key = "${userId}:${request.requestURI}"
        val bucket = rateLimitConfig.resolveBucket(key)
        
        if (!bucket.tryConsume(1)) {
            throw ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate limit exceeded. Please try again later."
            )
        }
    }
    
    fun checkRateLimitForEndpoint(endpoint: String, userId: String): Boolean {
        val key = "${userId}:${endpoint}"
        val bucket = rateLimitConfig.resolveBucket(key)
        return bucket.tryConsume(1)
    }
}
