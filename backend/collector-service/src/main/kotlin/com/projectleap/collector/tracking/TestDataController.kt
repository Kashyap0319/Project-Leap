package com.projectleap.collector.tracking

import com.projectleap.collector.dto.LogRequest
import com.projectleap.collector.logs.service.LogService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import kotlin.random.Random

/**
 * Test endpoint to generate sample API logs for testing the dashboard
 * This helps populate the dashboard with realistic data
 */
@RestController
@RequestMapping("/api/test")
class TestDataController(
    private val logService: LogService
) {
    
    @PostMapping("/generate-logs")
    fun generateTestLogs(
        @RequestParam(defaultValue = "50") count: Int,
        @RequestParam(defaultValue = "orders-service") service: String
    ): ResponseEntity<Map<String, Any>> {
        val endpoints = listOf(
            "/api/orders",
            "/api/orders/{id}",
            "/api/products",
            "/api/products/{id}",
            "/api/users",
            "/api/users/{id}",
            "/api/payments",
            "/api/inventory"
        )
        
        val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
        val statusCodes = listOf(200, 200, 200, 201, 400, 404, 500, 503) // More 200s for realism
        val services = listOf("orders-service", "products-service", "users-service", "payments-service", "inventory-service")
        
        val generated = mutableListOf<LogRequest>()
        val now = Instant.now()
        
        repeat(count) {
            val endpoint = endpoints.random()
            val method = methods.random()
            val statusCode = statusCodes.random()
            
            // Generate realistic latency
            val latencyMs = when {
                statusCode >= 500 -> Random.nextLong(600, 2000) // Slow for errors
                statusCode == 404 -> Random.nextLong(50, 200) // Fast for not found
                method == "GET" -> Random.nextLong(10, 300) // Fast for GET
                method == "POST" -> Random.nextLong(100, 600) // Slower for POST
                else -> Random.nextLong(50, 400)
            }
            
            val requestSize = when (method) {
                "GET", "DELETE" -> Random.nextLong(0, 500)
                else -> Random.nextLong(100, 5000)
            }
            
            val responseSize = when {
                statusCode == 404 -> Random.nextLong(0, 100)
                method == "GET" -> Random.nextLong(500, 10000)
                else -> Random.nextLong(100, 2000)
            }
            
            val rateLimited = Random.nextDouble() < 0.05 // 5% chance of rate limit
            val serviceName = if (service == "random") services.random() else service
            
            // Generate timestamp within last 24 hours
            val timestamp = now.minusSeconds(Random.nextLong(0, 86400))
            
            val logRequest = LogRequest(
                service = serviceName,
                endpoint = endpoint,
                method = method,
                statusCode = statusCode,
                latencyMs = latencyMs,
                requestSize = requestSize,
                responseSize = responseSize,
                rateLimited = rateLimited,
                timestamp = timestamp
            )
            
            try {
                logService.saveLog(logRequest)
                generated.add(logRequest)
            } catch (e: Exception) {
                // Skip on error
            }
        }
        
        return ResponseEntity.ok(mapOf(
            "message" to "Generated ${generated.size} test logs",
            "count" to generated.size,
            "service" to service
        ))
    }
    
    @PostMapping("/generate-realistic")
    fun generateRealisticScenario(): ResponseEntity<Map<String, Any>> {
        val services = listOf("orders-service", "products-service", "users-service")
        var totalGenerated = 0
        
        services.forEach { serviceName ->
            // Generate mix of successful and problematic requests
            repeat(30) {
                val latency = Random.nextLong(10, 200)
                val statusCode = if (Random.nextDouble() < 0.1) 500 else 200
                
                val logRequest = LogRequest(
                    service = serviceName,
                    endpoint = "/api/${serviceName.split("-")[0]}",
                    method = "GET",
                    statusCode = statusCode,
                    latencyMs = latency,
                    requestSize = Random.nextLong(0, 500),
                    responseSize = Random.nextLong(500, 5000),
                    rateLimited = false,
                    timestamp = Instant.now().minusSeconds(Random.nextLong(0, 3600))
                )
                
                try {
                    logService.saveLog(logRequest)
                    totalGenerated++
                } catch (e: Exception) {
                    // Skip
                }
            }
        }
        
        // Generate some slow APIs
        repeat(10) {
            val logRequest = LogRequest(
                service = services.random(),
                endpoint = "/api/heavy-operation",
                method = "POST",
                statusCode = 200,
                latencyMs = Random.nextLong(600, 1500),
                requestSize = Random.nextLong(1000, 5000),
                responseSize = Random.nextLong(500, 2000),
                rateLimited = false,
                timestamp = Instant.now().minusSeconds(Random.nextLong(0, 7200))
            )
            
            try {
                logService.saveLog(logRequest)
                totalGenerated++
            } catch (e: Exception) {
                // Skip
            }
        }
        
        return ResponseEntity.ok(mapOf(
            "message" to "Generated realistic test scenario",
            "totalLogs" to totalGenerated
        ))
    }
}

