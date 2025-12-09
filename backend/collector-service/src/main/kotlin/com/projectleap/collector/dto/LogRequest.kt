package com.projectleap.collector.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class LogRequest(
    @field:NotBlank val service: String,
    @field:NotBlank val endpoint: String,
    @field:NotBlank val method: String,
    @field:NotNull val statusCode: Int,
    @field:NotNull val latencyMs: Long,
    @field:NotNull val requestSize: Long,
    @field:NotNull val responseSize: Long,
    val timestamp: Instant? = null
)
