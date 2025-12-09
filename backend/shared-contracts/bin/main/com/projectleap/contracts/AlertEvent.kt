package com.projectleap.contracts

import kotlinx.serialization.Serializable

@Serializable
data class AlertEvent(
    val type: AlertType,
    val message: String,
    val service: String,
    val endpoint: String,
    val triggeredAt: Long,
    val severity: Severity = Severity.MEDIUM
)

enum class AlertType { LATENCY, STATUS_5XX, RATE_LIMIT }
enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
