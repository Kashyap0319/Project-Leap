package com.orders

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Instant

@Component
class TrackingInterceptor(
    private val trackingSender: TrackingSender
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        request.setAttribute("startTime", System.currentTimeMillis())
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        try {
            val startTime = request.getAttribute("startTime") as? Long ?: return
            val latency = System.currentTimeMillis() - startTime

            // Calculate response size from headers if available
            val responseSize = response.getHeader("Content-Length")?.toLongOrNull() 
                ?: (if (response.bufferSize > 0) response.bufferSize.toLong() else 0)
            
            val log = ApiLog(
                service = "orders-service",
                endpoint = request.requestURI,
                method = request.method,
                requestSize = request.contentLengthLong.takeIf { it > 0 } ?: 0,
                responseSize = responseSize,
                status = response.status,
                latency = latency,
                timestamp = Instant.now().toString(),
                rateLimited = response.status == 429
            )

            trackingSender.send(log)
        } catch (e: Exception) {
            // Silently fail - don't break the request flow
        }
    }
}

