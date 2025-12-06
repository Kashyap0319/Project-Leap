package com.projectleap.contracts

import kotlinx.serialization.Serializable

@Serializable
data class Incident(
    val id: String? = null,
    val service: String,
    val endpoint: String,
    val type: AlertType,
    val status: IncidentStatus = IncidentStatus.OPEN,
    val firstSeen: Long,
    val lastSeen: Long,
    val occurrences: Long = 1,
    val severity: Severity = Severity.MEDIUM,
    val version: Long? = null
)

enum class IncidentStatus { OPEN, RESOLVED }
