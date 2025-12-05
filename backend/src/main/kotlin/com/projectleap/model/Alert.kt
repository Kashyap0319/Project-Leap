package com.projectleap.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "alerts")
data class Alert(
    @Id
    val id: String? = null,
    
    val type: AlertType,
    
    val severity: AlertSeverity,
    
    val title: String,
    
    val description: String,
    
    val source: String,
    
    val threshold: Double,
    
    val currentValue: Double,
    
    val status: AlertStatus = AlertStatus.OPEN,
    
    val createdAt: Instant = Instant.now(),
    
    val resolvedAt: Instant? = null,
    
    val resolvedBy: String? = null,
    
    val resolution: String? = null,
    
    @Version
    val version: Long? = null
)

enum class AlertType {
    ERROR_RATE, RATE_LIMIT, SYSTEM, CUSTOM
}

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class AlertStatus {
    OPEN, ACKNOWLEDGED, RESOLVED, DISMISSED
}
