package com.projectleap.service

import com.projectleap.model.Alert
import com.projectleap.model.AlertSeverity
import com.projectleap.model.AlertStatus
import com.projectleap.model.AlertType
import com.projectleap.model.LogLevel
import com.projectleap.repository.logs.LogRepository
import com.projectleap.repository.metadata.AlertRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AlertService(
    private val alertRepository: AlertRepository,
    private val logRepository: LogRepository
) {
    
    @Value("\${alert.threshold.error-rate}")
    private var errorRateThreshold: Double = 10.0
    
    @Value("\${alert.threshold.time-window-minutes}")
    private var timeWindowMinutes: Long = 5
    
    fun getAllAlerts(): List<Alert> {
        return alertRepository.findAll()
    }
    
    fun getAlertsByStatus(status: AlertStatus): List<Alert> {
        return alertRepository.findByStatus(status)
    }
    
    fun getAlertById(id: String): Alert? {
        return alertRepository.findById(id).orElse(null)
    }
    
    fun createAlert(alert: Alert): Alert {
        return alertRepository.save(alert)
    }
    
    fun resolveAlert(id: String, resolvedBy: String, resolution: String): Alert {
        val alert = alertRepository.findById(id)
            .orElseThrow { NoSuchElementException("Alert not found: $id") }
        
        try {
            val updated = alert.copy(
                status = AlertStatus.RESOLVED,
                resolvedAt = Instant.now(),
                resolvedBy = resolvedBy,
                resolution = resolution
            )
            return alertRepository.save(updated)
        } catch (e: OptimisticLockingFailureException) {
            throw IllegalStateException("Alert was modified by another process. Please retry.", e)
        }
    }
    
    fun dismissAlert(id: String): Alert {
        val alert = alertRepository.findById(id)
            .orElseThrow { NoSuchElementException("Alert not found: $id") }
        
        try {
            val updated = alert.copy(status = AlertStatus.DISMISSED)
            return alertRepository.save(updated)
        } catch (e: OptimisticLockingFailureException) {
            throw IllegalStateException("Alert was modified by another process. Please retry.", e)
        }
    }
    
    fun checkErrorRateAndCreateAlert(source: String) {
        val endTime = Instant.now()
        val startTime = endTime.minus(timeWindowMinutes, ChronoUnit.MINUTES)
        
        val errorCount = logRepository.countByLevelAndTimestampBetween(LogLevel.ERROR, startTime, endTime)
        val fatalCount = logRepository.countByLevelAndTimestampBetween(LogLevel.FATAL, startTime, endTime)
        val totalErrors = errorCount + fatalCount
        
        if (totalErrors > errorRateThreshold) {
            val existingAlerts = alertRepository.findByStatusAndType(AlertStatus.OPEN, AlertType.ERROR_RATE)
            val hasOpenAlert = existingAlerts.any { it.source == source }
            
            if (!hasOpenAlert) {
                val alert = Alert(
                    type = AlertType.ERROR_RATE,
                    severity = when {
                        totalErrors > errorRateThreshold * 3 -> AlertSeverity.CRITICAL
                        totalErrors > errorRateThreshold * 2 -> AlertSeverity.HIGH
                        else -> AlertSeverity.MEDIUM
                    },
                    title = "High Error Rate Detected",
                    description = "Source '$source' has $totalErrors errors in the last $timeWindowMinutes minutes",
                    source = source,
                    threshold = errorRateThreshold,
                    currentValue = totalErrors.toDouble()
                )
                alertRepository.save(alert)
            }
        }
    }
}
