package com.orders

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import kotlin.random.Random

@Component
class AutoPingScheduler(
    @Value("\${server.port:9000}") private val serverPort: Int
) {
    private val logger = LoggerFactory.getLogger(AutoPingScheduler::class.java)
    private val rest = RestTemplate()
    
    private val endpoints = listOf(
        "http://localhost:$serverPort/orders/create",
        "http://localhost:$serverPort/orders/list",
        "http://localhost:$serverPort/orders/123",
        "http://localhost:$serverPort/orders/456",
        "http://localhost:$serverPort/orders/payment/process",
        "http://localhost:$serverPort/orders/inventory/check"
    )

    @Scheduled(fixedDelay = 3000) // Every 3 seconds
    fun autoHit() {
        try {
            val url = endpoints.random()
            // All endpoints support GET, so just use GET for simplicity
            rest.getForObject(url, Map::class.java)
            logger.debug("Auto-pinged: GET $url")
        } catch (e: Exception) {
            // Expected - some endpoints may fail (5xx errors are tracked too!)
            // This is intentional to simulate real-world error scenarios
            logger.debug("Auto-ping failed (this is expected for error simulation): ${e.message}")
        }
    }
}

