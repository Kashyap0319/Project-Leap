package com.projectleap.tracker

import com.projectleap.contracts.LogEvent
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper
import java.time.Instant

class ServerTrackingFilter(
    private val serviceName: String,
    private val rateLimiter: RateLimiter,
    private val batcher: LogBatcher
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val start = Instant.now().toEpochMilli()
        val allowed = rateLimiter.tryConsume()
        val wrappedResponse = ContentCachingResponseWrapper(response)
        try {
            filterChain.doFilter(request, wrappedResponse)
        } finally {
            val end = Instant.now().toEpochMilli()
            val status = wrappedResponse.status
            val event = LogEvent(
                service = serviceName,
                endpoint = request.requestURI,
                method = request.method,
                status = status,
                latencyMs = end - start,
                rateLimited = !allowed,
                timestamp = start,
                requestId = request.getHeader("X-Request-Id"),
                requestBytes = request.contentLengthLong.takeIf { it >= 0 },
                responseBytes = wrappedResponse.contentSize.toLong()
            )
            batcher.enqueue(event)
            wrappedResponse.copyBodyToResponse()
        }
    }
}
