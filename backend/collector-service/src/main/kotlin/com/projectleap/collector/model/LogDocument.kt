package com.projectleap.collector.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

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
    @Field("requestBytes")
    @JsonProperty("requestSizeBytes")
    val requestSizeBytes: Long? = null,
    @Field("responseBytes")
    @JsonProperty("responseSizeBytes")
    val responseSizeBytes: Long? = null
)
