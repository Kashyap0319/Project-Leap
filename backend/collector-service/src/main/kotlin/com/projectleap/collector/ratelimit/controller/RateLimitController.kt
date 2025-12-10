package com.projectleap.collector.ratelimit.controller

import com.projectleap.collector.ratelimit.RateLimitConfigService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rate-limit")
class RateLimitController(
    private val rateLimitConfigService: RateLimitConfigService
) {
    
    @GetMapping
    fun getRateLimitConfigs(): ResponseEntity<List<Map<String, Any>>> {
        val configs = rateLimitConfigService.getAllConfigs().map { (service, limit) ->
            mapOf(
                "service" to service,
                "limit" to limit
            )
        }
        return ResponseEntity.ok(configs)
    }
    
    @PostMapping
    fun setRateLimitOverride(@RequestBody body: Map<String, Any>): ResponseEntity<Map<String, Any>> {
        val service = body["service"] as? String ?: throw IllegalArgumentException("Service is required")
        val limit = (body["limit"] as? Number)?.toLong() ?: throw IllegalArgumentException("Limit is required")
        
        rateLimitConfigService.setLimitForService(service, limit)
        
        return ResponseEntity.ok(mapOf(
            "service" to service,
            "limit" to limit,
            "message" to "Rate limit override set successfully"
        ))
    }
    
    @GetMapping("/{service}")
    fun getRateLimitForService(@PathVariable service: String): ResponseEntity<Map<String, Any>> {
        val limit = rateLimitConfigService.getLimitForService(service)
        return ResponseEntity.ok(mapOf(
            "service" to service,
            "limit" to limit
        ))
    }
}

