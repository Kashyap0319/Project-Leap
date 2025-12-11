package com.projectleap.collector.tracking

import com.projectleap.collector.dto.LogRequest
import com.projectleap.collector.logs.service.LogService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Instant

/**
 * Interceptor to track all API calls to the collector service
 * This generates logs that appear on the dashboard
 */
@Component
class ApiTrackingInterceptor(
    private val logService: LogService
) : HandlerInterceptor {
    
    private val logger = LoggerFactory.getLogger(ApiTrackingInterceptor::class.java)
    private val requestStartTime = ThreadLocal<Long>()
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // Store start time for latency calculation
        requestStartTime.set(System.currentTimeMillis())
        return true
    }
    
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        try {
            val startTime = requestStartTime.get()
            if (startTime == null) return
            
            val latencyMs = System.currentTimeMillis() - startTime
            val statusCode = response.status
            val endpoint = request.requestURI
            val method = request.method
            
            // Extract service name from header or use default
            val serviceName = request.getHeader("X-Service-Name") 
                ?: "collector-service"
            
            // Calculate request/response sizes
            val requestSize = request.contentLengthLong.takeIf { it > 0 } ?: 0
            val responseSize = response.getHeader("Content-Length")?.toLongOrNull() ?: 0
            
            // Skip logging for health checks and static resources
            if (endpoint.startsWith("/health") || 
                endpoint.startsWith("/favicon") ||
                endpoint.startsWith("/actuator")) {
                return
            }
            
            // Create log entry
            val logRequest = LogRequest(
                service = serviceName,
                endpoint = endpoint,
                method = method,
                statusCode = statusCode,
                latencyMs = latencyMs,
                requestSize = requestSize,
                responseSize = responseSize,
                rateLimited = statusCode == 429,
                timestamp = Instant.now()
            )
            
            // Save log asynchronously to avoid blocking
            try {
                logService.saveLog(logRequest)
            } catch (e: Exception) {
                logger.error("Failed to save API log", e)
            }
            
        } catch (e: Exception) {
            logger.error("Error in API tracking interceptor", e)
        } finally {
            requestStartTime.remove()
        }
    }
}

