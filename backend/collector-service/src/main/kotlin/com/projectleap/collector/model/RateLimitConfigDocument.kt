package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("rate_limit_configs")
data class RateLimitConfigDocument(
    @Id val id: String? = null,
    @Indexed(unique = true) val service: String,
    val limitPerSecond: Long,
    val burst: Long = limitPerSecond,
    val updatedAt: Long = System.currentTimeMillis()
)
