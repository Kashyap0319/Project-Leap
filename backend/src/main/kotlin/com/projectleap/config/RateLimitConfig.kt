package com.projectleap.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Configuration
class RateLimitConfig {
    
    @Value("\${ratelimit.capacity}")
    private var capacity: Long = 100
    
    @Value("\${ratelimit.refill-tokens}")
    private var refillTokens: Long = 10
    
    @Value("\${ratelimit.refill-duration-seconds}")
    private var refillDurationSeconds: Long = 1
    
    private val cache = ConcurrentHashMap<String, Bucket>()
    
    fun resolveBucket(key: String): Bucket {
        return cache.computeIfAbsent(key) { newBucket() }
    }
    
    private fun newBucket(): Bucket {
        val refill = Refill.intervally(refillTokens, Duration.ofSeconds(refillDurationSeconds))
        val limit = Bandwidth.classic(capacity, refill)
        return Bucket.builder()
            .addLimit(limit)
            .build()
    }
    
    fun resetBucket(key: String) {
        cache.remove(key)
    }
}
