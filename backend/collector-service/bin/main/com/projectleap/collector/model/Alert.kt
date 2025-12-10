package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "alerts")
data class Alert(
    @Id val id: String? = null,
    val message: String,
    val service: String? = null,
    val endpoint: String? = null,
    val type: String, // "LATENCY", "ERROR", "RATE_LIMIT"
    val severity: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val detectedAt: Instant = Instant.now(),
    val resolved: Boolean = false,
    val resolvedAt: Instant? = null
)
