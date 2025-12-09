package com.projectleap.contracts

import kotlinx.serialization.Serializable

@Serializable
data class LogEvent(
    val service: String,
    val endpoint: String,
    val status: Int,
    val latencyMs: Long,
    val rateLimited: Boolean,
    val timestamp: Long,
    val requestId: String? = null,
    val method: String = "GET",
    val requestBytes: Long? = null,
    val responseBytes: Long? = null
)
