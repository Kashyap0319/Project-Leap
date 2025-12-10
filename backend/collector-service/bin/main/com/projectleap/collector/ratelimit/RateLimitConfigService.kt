package com.projectleap.collector.ratelimit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * Service to manage rate limit configurations per service
 * Supports both rate-limit.default and monitoring.rateLimit.* formats
 */
@Service
class RateLimitConfigService {
    
    private val configs = ConcurrentHashMap<String, Long>()
    
    @Value("\${rate-limit.default:100}")
    private val defaultLimit: Long = 100
    
    @Value("\${monitoring.rateLimit.default:100}")
    private val monitoringDefaultLimit: Long = 100
    
    @Autowired
    lateinit var rateLimiter: RateLimiter
    
    init {
        // Load from config if available (use monitoring format if available, else fallback)
        val effectiveDefault = if (monitoringDefaultLimit != 100L) monitoringDefaultLimit else defaultLimit
        configs["default"] = effectiveDefault
    }
    
    @EventListener(ApplicationReadyEvent::class)
    fun loadRateLimitConfigs() {
        // Load per-service configs from application.yml if configured
        // Format: monitoring.rateLimit.service: <serviceName>, limit: <limit>
        // This would require additional @ConfigurationProperties, but for now
        // we support dynamic setting via API
    }
    
    fun getLimitForService(service: String): Long {
        return configs[service] ?: configs["default"] ?: defaultLimit
    }
    
    fun setLimitForService(service: String, limit: Long) {
        configs[service] = limit
        // Update rate limiter
        rateLimiter.setLimit(service, limit)
    }
    
    fun getAllConfigs(): Map<String, Long> {
        return configs.toMap()
    }
}

