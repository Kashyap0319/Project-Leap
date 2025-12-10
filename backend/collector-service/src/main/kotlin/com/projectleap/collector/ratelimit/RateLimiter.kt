package com.projectleap.collector.ratelimit

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

/**
 * Token-bucket rate limiter implementation
 * Each service gets its own bucket with configurable capacity and refill rate
 */
@Component
class RateLimiter {
    
    data class Bucket(
        var capacity: Long,
        var refillRate: Long, // tokens per second
        var tokens: AtomicLong = AtomicLong(capacity),
        var lastRefill: Long = System.currentTimeMillis()
    )
    
    private val buckets = ConcurrentHashMap<String, Bucket>()
    private val defaultLimit = 100L // 100 req/sec default
    
    /**
     * Check if request is allowed for the given service
     * @return true if allowed, false if rate limited
     */
    fun isAllowed(service: String, limit: Long = defaultLimit): Boolean {
        val bucket = buckets.getOrPut(service) {
            Bucket(capacity = limit, refillRate = limit)
        }
        
        // Update limit if changed
        if (bucket.capacity != limit) {
            bucket.capacity = limit
            bucket.refillRate = limit
        }
        
        return synchronized(bucket) {
            refillTokens(bucket)
            if (bucket.tokens.get() > 0) {
                bucket.tokens.decrementAndGet()
                true
            } else {
                false
            }
        }
    }
    
    /**
     * Refill tokens based on elapsed time
     */
    private fun refillTokens(bucket: Bucket) {
        val now = System.currentTimeMillis()
        val elapsed = (now - bucket.lastRefill) / 1000.0 // seconds
        
        if (elapsed > 0) {
            val tokensToAdd = (elapsed * bucket.refillRate).toLong()
            val newTokens = min(bucket.capacity, bucket.tokens.get() + tokensToAdd)
            bucket.tokens.set(newTokens)
            bucket.lastRefill = now
        }
    }
    
    /**
     * Set rate limit for a service
     */
    fun setLimit(service: String, limit: Long) {
        buckets.getOrPut(service) {
            Bucket(capacity = limit, refillRate = limit)
        }.apply {
            capacity = limit
            refillRate = limit
        }
    }
    
    /**
     * Get current limit for a service
     */
    fun getLimit(service: String): Long {
        return buckets[service]?.capacity ?: defaultLimit
    }
    
    /**
     * Reset bucket for a service (for testing)
     */
    fun reset(service: String) {
        buckets.remove(service)
    }
}

