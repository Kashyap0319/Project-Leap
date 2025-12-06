package com.projectleap.collector.service

import com.projectleap.collector.model.AlertDocument
import com.projectleap.contracts.AlertType
import com.projectleap.contracts.LogEvent
import com.projectleap.contracts.Severity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AlertService(
    @Qualifier("metaTemplate") private val metaTemplate: MongoTemplate,
    private val incidentService: IncidentService
) {
    fun evaluateAndPersist(events: List<LogEvent>) {
        val alerts = events.flatMap { evaluate(it) }
        if (alerts.isEmpty()) return
        metaTemplate.insert(alerts, AlertDocument::class.java)
        alerts.forEach { incidentService.touchIncident(it) }
    }

    private fun evaluate(event: LogEvent): List<AlertDocument> {
        val out = mutableListOf<AlertDocument>()
        val now = Instant.now().toEpochMilli()
        if (event.latencyMs > 500) out += alert(AlertType.LATENCY, event, now, Severity.MEDIUM)
        if (event.status >= 500) out += alert(AlertType.STATUS_5XX, event, now, Severity.HIGH)
        if (event.rateLimited) out += alert(AlertType.RATE_LIMIT, event, now, Severity.LOW)
        return out
    }

    private fun alert(type: AlertType, event: LogEvent, ts: Long, severity: Severity) =
        AlertDocument(
            type = type,
            message = "${type.name} on ${event.service}${event.endpoint}",
            service = event.service,
            endpoint = event.endpoint,
            triggeredAt = ts,
            severity = severity
        )
}
