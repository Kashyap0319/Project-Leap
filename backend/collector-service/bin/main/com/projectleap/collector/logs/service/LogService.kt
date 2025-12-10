package com.projectleap.collector.logs.service

import com.projectleap.collector.alerts.service.AlertService
import com.projectleap.collector.dto.LogRequest
import com.projectleap.collector.logs.repository.LogEntryRepository
import com.projectleap.collector.model.LogEntry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class LogService(
    private val logEntryRepository: LogEntryRepository,
    private val alertService: AlertService
) {

    @Transactional
    fun saveLog(request: LogRequest): LogEntry {
        val logEntry = LogEntry(
            service = request.service,
            endpoint = request.endpoint,
            method = request.method,
            statusCode = request.statusCode,
            latencyMs = request.latencyMs,
            requestSize = request.requestSize,
            responseSize = request.responseSize,
            rateLimited = request.rateLimited,
            timestamp = request.timestamp ?: Instant.now()
        )
        val saved = logEntryRepository.save(logEntry)
        
        // Evaluate alert rules
        evaluateAlertRules(saved)
        
        return saved
    }
    
    private fun evaluateAlertRules(logEntry: LogEntry) {
        // Rule 1: Latency > 500ms
        if (logEntry.latencyMs != null && logEntry.latencyMs!! > 500) {
            alertService.createAlert(
                message = "High latency detected: ${logEntry.latencyMs}ms for ${logEntry.endpoint ?: "unknown endpoint"}",
                service = logEntry.service,
                endpoint = logEntry.endpoint,
                type = "LATENCY",
                severity = if (logEntry.latencyMs!! > 1000) "CRITICAL" else "WARNING"
            )
        }
        
        // Rule 2: Status >= 500
        if (logEntry.statusCode != null && logEntry.statusCode!! >= 500) {
            alertService.createAlert(
                message = "Server error: ${logEntry.statusCode} for ${logEntry.endpoint ?: "unknown endpoint"}",
                service = logEntry.service,
                endpoint = logEntry.endpoint,
                type = "ERROR",
                severity = "CRITICAL"
            )
        }
        
        // Rule 3: Rate limit hit
        if (logEntry.rateLimited == true) {
            alertService.createAlert(
                message = "Rate limit exceeded for ${logEntry.service ?: "unknown service"}",
                service = logEntry.service,
                endpoint = logEntry.endpoint,
                type = "RATE_LIMIT",
                severity = "MEDIUM"
            )
        }
    }

    fun getLogs(service: String?, from: Instant?, to: Instant?, pageable: Pageable): Page<LogEntry> {
        return when {
            service != null && from != null && to != null ->
                logEntryRepository.findByServiceAndTimestampBetween(service, from, to, pageable)
            from != null && to != null ->
                logEntryRepository.findByTimestampBetween(from, to, pageable)
            service != null ->
                logEntryRepository.findByService(service, pageable)
            else ->
                logEntryRepository.findAll(pageable)
        }
    }
}
