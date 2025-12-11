package com.orders

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant

@Service
class TrackingSender(
    @Value("\${monitoring.collectorUrl:http://localhost:8080/api/logs}") private val collectorUrl: String
) {
    private val logger = LoggerFactory.getLogger(TrackingSender::class.java)
    private val rest = RestTemplate()

    fun send(log: ApiLog) {
        try {
            // Parse timestamp string to Instant for proper serialization
            val timestampInstant = try {
                java.time.Instant.parse(log.timestamp)
            } catch (e: Exception) {
                java.time.Instant.now()
            }
            
            val logRequest = mapOf(
                "service" to log.service,
                "endpoint" to log.endpoint,
                "method" to log.method,
                "statusCode" to log.status,
                "latencyMs" to log.latency,
                "requestSize" to log.requestSize,
                "responseSize" to log.responseSize,
                "rateLimited" to log.rateLimited,
                "timestamp" to timestampInstant.toString()
            )
            
            rest.postForObject(collectorUrl, logRequest, Any::class.java)
        } catch (e: Exception) {
            logger.debug("Failed to send log to collector: ${e.message}", e)
        }
    }
}

data class ApiLog(
    val service: String,
    val endpoint: String,
    val method: String,
    val requestSize: Long,
    val responseSize: Long,
    val status: Int,
    val latency: Long,
    val timestamp: String,
    val rateLimited: Boolean = false
)

