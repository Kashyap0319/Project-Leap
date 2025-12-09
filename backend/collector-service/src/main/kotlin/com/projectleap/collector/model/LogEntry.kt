package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "logs")
data class LogEntry(
    @Id val id: String? = null,
    val service: String,
    val endpoint: String,
    val method: String,
    val statusCode: Int,
    val latencyMs: Long,
    val requestSize: Long,
    val responseSize: Long,
    val timestamp: Instant = Instant.now()
)
