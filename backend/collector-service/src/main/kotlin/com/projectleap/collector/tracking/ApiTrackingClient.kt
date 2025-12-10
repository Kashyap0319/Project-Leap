package com.projectleap.collector.tracking

import com.projectleap.collector.dto.LogRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

/**
 * API Tracking Client - Library for services to track their API calls
 * Usage: Add this as a dependency and configure in your service
 * 
 * Includes built-in rate limiter that allows requests to continue
 * but logs rate-limit-hit events to collector
 */
class ApiTrackingClient(
    private val collectorUrl: String,
    private val serviceName: String,
    private val jwtSecret: String? = null,
    private val rateLimit: Long = 100L // Default 100 req/sec
) {
    
    private val logger = LoggerFactory.getLogger(ApiTrackingClient::class.java)
    private val restTemplate = RestTemplate()
    
    // Built-in rate limiter (token bucket)
    private data class Bucket(
        var capacity: Long,
        var refillRate: Long,
        var tokens: AtomicLong = AtomicLong(capacity),
        var lastRefill: Long = System.currentTimeMillis()
    )
    
    private val rateLimitBucket = Bucket(capacity = rateLimit, refillRate = rateLimit)
    
    /**
     * Track a single API call
     * Includes rate limiting - if limit exceeded, logs rate-limit-hit but allows tracking to continue
     */
    fun trackApiCall(
        endpoint: String,
        method: String,
        statusCode: Int,
        latencyMs: Long,
        requestSize: Long = 0,
        responseSize: Long = 0,
        rateLimited: Boolean = false
    ) {
        try {
            // Check rate limit (but don't block - assignment requirement)
            val wasRateLimited = checkRateLimit()
            val finalRateLimited = rateLimited || wasRateLimited
            
            val logRequest = LogRequest(
                service = serviceName,
                endpoint = endpoint,
                method = method,
                statusCode = statusCode,
                latencyMs = latencyMs,
                requestSize = requestSize,
                responseSize = responseSize,
                rateLimited = finalRateLimited,
                timestamp = Instant.now()
            )
            
            sendToCollector(listOf(logRequest))
            
            // If rate limited, log additional rate-limit-hit event
            if (wasRateLimited) {
                logRateLimitHit(endpoint, method)
            }
        } catch (e: Exception) {
            logger.error("Failed to track API call", e)
        }
    }
    
    /**
     * Check rate limit using token bucket
     * Returns true if rate limited, but doesn't block the request
     */
    private fun checkRateLimit(): Boolean {
        synchronized(rateLimitBucket) {
            val now = System.currentTimeMillis()
            val elapsed = (now - rateLimitBucket.lastRefill) / 1000.0
            
            if (elapsed > 0) {
                val tokensToAdd = (elapsed * rateLimitBucket.refillRate).toLong()
                val newTokens = min(rateLimitBucket.capacity, rateLimitBucket.tokens.get() + tokensToAdd)
                rateLimitBucket.tokens.set(newTokens)
                rateLimitBucket.lastRefill = now
            }
            
            return if (rateLimitBucket.tokens.get() > 0) {
                rateLimitBucket.tokens.decrementAndGet()
                false // Not rate limited
            } else {
                true // Rate limited (but request continues)
            }
        }
    }
    
    /**
     * Log rate-limit-hit event to collector
     */
    private fun logRateLimitHit(endpoint: String, method: String) {
        try {
            val rateLimitLog = LogRequest(
                service = serviceName,
                endpoint = endpoint,
                method = method,
                statusCode = 429,
                latencyMs = 0,
                requestSize = 0,
                responseSize = 0,
                rateLimited = true,
                timestamp = Instant.now()
            )
            sendToCollector(listOf(rateLimitLog))
        } catch (e: Exception) {
            logger.error("Failed to log rate-limit-hit event", e)
        }
    }
    
    /**
     * Track multiple API calls in batch
     */
    fun trackApiCallsBatch(logs: List<LogRequest>) {
        try {
            sendToCollector(logs)
        } catch (e: Exception) {
            logger.error("Failed to track API calls batch", e)
        }
    }
    
    private fun sendToCollector(logs: List<LogRequest>) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        
        // Add JWT if provided
        jwtSecret?.let {
            // In production, generate JWT token here
            // For now, just send without auth (collector will handle)
        }
        
        val entity = HttpEntity(logs, headers)
        
        val url = if (logs.size == 1) {
            "$collectorUrl/api/logs"
        } else {
            "$collectorUrl/api/logs/batch"
        }
        
        restTemplate.postForObject(url, entity, Any::class.java)
    }
    
    companion object {
        /**
         * Create a builder for ApiTrackingClient
         */
        fun builder(): Builder = Builder()
    }
    
    class Builder {
        private var collectorUrl: String = "http://localhost:8080"
        private var serviceName: String = "unknown"
        private var jwtSecret: String? = null
        private var rateLimit: Long = 100L
        
        fun collectorUrl(url: String) = apply { this.collectorUrl = url }
        fun serviceName(name: String) = apply { this.serviceName = name }
        fun jwtSecret(secret: String) = apply { this.jwtSecret = secret }
        fun rateLimit(limit: Long) = apply { this.rateLimit = limit }
        
        fun build(): ApiTrackingClient = ApiTrackingClient(collectorUrl, serviceName, jwtSecret, rateLimit)
    }
}

