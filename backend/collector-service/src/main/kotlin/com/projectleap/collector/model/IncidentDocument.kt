package com.projectleap.collector.model

import com.projectleap.contracts.AlertType
import com.projectleap.contracts.IncidentStatus
import com.projectleap.contracts.Severity
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document

@Document("incidents")
data class IncidentDocument(
    @Id val id: String? = null,
    val service: String,
    val endpoint: String,
    val type: AlertType,
    val status: IncidentStatus = IncidentStatus.OPEN,
    val firstSeen: Long,
    val lastSeen: Long,
    val occurrences: Long = 1,
    val severity: Severity = Severity.MEDIUM,
    @Version val version: Long = 0,
    val resolvedAt: Long? = null
)
