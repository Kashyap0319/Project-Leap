package com.projectleap.collector.model

import com.projectleap.contracts.AlertType
import com.projectleap.contracts.Severity
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("alerts")
data class AlertDocument(
    @Id val id: String? = null,
    val type: AlertType,
    val message: String,
    val service: String,
    val endpoint: String,
    val triggeredAt: Long,
    val severity: Severity,
    @get:JsonProperty("detectedAt") val detectedAt: Long = triggeredAt
)
