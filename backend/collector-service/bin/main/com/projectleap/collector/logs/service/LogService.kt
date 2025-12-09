package com.projectleap.collector.logs.service

import com.projectleap.collector.dto.LogRequest
import com.projectleap.collector.logs.repository.LogEntryRepository
import com.projectleap.collector.model.LogEntry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class LogService(
    private val logEntryRepository: LogEntryRepository
) {

    fun saveLog(request: LogRequest): LogEntry {
        val logEntry = LogEntry(
            service = request.service,
            endpoint = request.endpoint,
            method = request.method,
            statusCode = request.statusCode,
            latencyMs = request.latencyMs,
            requestSize = request.requestSize,
            responseSize = request.responseSize,
            timestamp = request.timestamp ?: Instant.now()
        )
        return logEntryRepository.save(logEntry)
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
