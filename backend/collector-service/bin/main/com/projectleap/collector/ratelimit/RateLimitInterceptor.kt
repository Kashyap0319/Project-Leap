package com.projectleap.collector.ratelimit

import com.projectleap.collector.logs.service.LogService
import com.projectleap.collector.model.LogEntry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Instant

/**
 * Interceptor to check rate limits and log rate-limit-hit events
 */
@Component
class RateLimitInterceptor(
    private val rateLimiter: RateLimiter,
    private val rateLimitConfigService: RateLimitConfigService
) : HandlerInterceptor {
    
    private val logger = LoggerFactory.getLogger(RateLimitInterceptor::class.java)
    
    @Autowired(required = false)
    private var logService: LogService? = null
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // Extract service name from request
        val serviceName = extractServiceName(request)
        val limit = rateLimitConfigService.getLimitForService(serviceName)
        
        // Check rate limit
        val allowed = rateLimiter.isAllowed(serviceName, limit)
        
        if (!allowed) {
            logger.warn("Rate limit exceeded for service: $serviceName (limit: $limit req/sec)")
            
            // Log rate-limit-hit event
            logService?.let { service ->
                try {
                    val logEntry = LogEntry(
                        service = serviceName,
                        endpoint = request.requestURI,
                        method = request.method,
                        statusCode = 429,
                        latencyMs = 0,
                        requestSize = request.contentLengthLong.takeIf { it > 0 } ?: 0,
                        responseSize = 0,
                        rateLimited = true,
                        timestamp = Instant.now()
                    )
                    service.saveLog(com.projectleap.collector.dto.LogRequest(
                        service = logEntry.service ?: "unknown",
                        endpoint = logEntry.endpoint ?: "",
                        method = logEntry.method ?: "GET",
                        statusCode = logEntry.statusCode ?: 429,
                        latencyMs = logEntry.latencyMs ?: 0,
                        requestSize = logEntry.requestSize ?: 0,
                        responseSize = logEntry.responseSize ?: 0,
                        rateLimited = true,
                        timestamp = logEntry.timestamp
                    ))
                } catch (e: Exception) {
                    logger.error("Failed to log rate-limit event", e)
                }
            }
            
            response.status = 429
            response.contentType = "application/json"
            response.writer.write("""{"error":"Rate limit exceeded","service":"$serviceName","limit":$limit}""")
            return false
        }
        
        return true
    }
    
    private fun extractServiceName(request: HttpServletRequest): String {
        // Try to get service name from header
        val serviceHeader = request.getHeader("X-Service-Name")
        if (!serviceHeader.isNullOrBlank()) {
            return serviceHeader
        }
        
        // Try to get from request attribute (set by client)
        val serviceAttr = request.getAttribute("serviceName") as? String
        if (!serviceAttr.isNullOrBlank()) {
            return serviceAttr
        }
        
        // Default to "unknown"
        return "unknown"
    }
}

