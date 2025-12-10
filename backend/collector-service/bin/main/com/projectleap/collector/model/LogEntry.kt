package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "logs")
data class LogEntry(
    @Id val id: String? = null,
    val service: String? = null,
    val endpoint: String? = null,
    val method: String? = null,
    val statusCode: Int? = null,
    val latencyMs: Long? = null,
    val requestSize: Long? = null,
    val responseSize: Long? = null,
    val rateLimited: Boolean? = false,
    val timestamp: Instant? = Instant.now()
)
