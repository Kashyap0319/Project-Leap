package com.projectleap.collector.alerts.service

import com.projectleap.collector.alerts.repository.AlertRepository
import com.projectleap.collector.model.Alert
import org.springframework.stereotype.Service

@Service
class AlertService(
    private val alertRepository: AlertRepository
) {

    fun createAlert(message: String): Alert =
        alertRepository.save(Alert(message = message))

    fun getActiveAlerts(): List<Alert> =
        alertRepository.findByResolved(false)

    fun resolveAlert(id: String): Alert {
        val alert = alertRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Alert not found") }
        val updated = alert.copy(resolved = true)
        return alertRepository.save(updated)
    }
}
