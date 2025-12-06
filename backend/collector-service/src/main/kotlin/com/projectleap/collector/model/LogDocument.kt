package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("logs")
data class LogDocument(
    @Id val id: String? = null,
    val service: String,
    val endpoint: String,
    val method: String,
    val status: Int,
    val latencyMs: Long,
    val rateLimited: Boolean,
    val timestamp: Long,
    val requestId: String? = null,
    val requestBytes: Long? = null,
    val responseBytes: Long? = null
)
