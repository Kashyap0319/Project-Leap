package com.projectleap.collector.alerts.service

import com.projectleap.collector.alerts.repository.AlertRepository
import com.projectleap.collector.model.Alert
import java.time.Instant
import org.springframework.stereotype.Service

@Service
class AlertService(
    private val alertRepository: AlertRepository
) {

    fun createAlert(
        message: String,
        service: String? = null,
        endpoint: String? = null,
        type: String,
        severity: String
    ): Alert {
        return alertRepository.save(
            Alert(
                message = message,
                service = service,
                endpoint = endpoint,
                type = type,
                severity = severity
            )
        )
    }

    fun getActiveAlerts(): List<Alert> =
        alertRepository.findByResolved(false)

    fun resolveAlert(id: String): Alert {
        val alert = alertRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Alert not found") }
        val updated = alert.copy(resolved = true, resolvedAt = Instant.now())
        return alertRepository.save(updated)
    }
}
