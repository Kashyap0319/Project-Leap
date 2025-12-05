package com.projectleap.service

import com.projectleap.model.LogEntry
import com.projectleap.model.LogLevel
import com.projectleap.repository.logs.LogRepository
import kotlinx.coroutines.*
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.Semaphore

@Service
class LogService(
    private val logRepository: LogRepository,
    private val alertService: AlertService
) {
    private val writeSemaphore = Semaphore(50) // Handle 50+ concurrent writes
    
    suspend fun createLog(logEntry: LogEntry): LogEntry = coroutineScope {
        writeSemaphore.acquire()
        try {
            val saved = withContext(Dispatchers.IO) {
                logRepository.save(logEntry)
            }
            
            // Trigger alert check asynchronously
            launch {
                checkAndCreateAlerts(logEntry)
            }
            
            saved
        } finally {
            writeSemaphore.release()
        }
    }
    
    suspend fun createLogsBulk(logEntries: List<LogEntry>): List<LogEntry> = coroutineScope {
        logEntries.map { logEntry ->
            async {
                createLog(logEntry)
            }
        }.awaitAll()
    }
    
    fun getLogs(
        level: LogLevel? = null,
        source: String? = null,
        startTime: Instant? = null,
        endTime: Instant? = null
    ): List<LogEntry> {
        return when {
            level != null && startTime != null && endTime != null -> 
                logRepository.findByLevelAndTimestampBetween(level, startTime, endTime)
            source != null && startTime != null && endTime != null -> 
                logRepository.findBySourceAndTimestampBetween(source, startTime, endTime)
            startTime != null && endTime != null -> 
                logRepository.findByTimestampBetween(startTime, endTime)
            level != null -> 
                logRepository.findByLevel(level)
            source != null -> 
                logRepository.findBySource(source)
            else -> 
                logRepository.findAll()
        }
    }
    
    fun getLogById(id: String): LogEntry? {
        return logRepository.findById(id).orElse(null)
    }
    
    fun updateLog(id: String, logEntry: LogEntry): LogEntry {
        val existing = logRepository.findById(id)
            .orElseThrow { NoSuchElementException("Log not found: $id") }
        
        try {
            val updated = existing.copy(
                level = logEntry.level,
                message = logEntry.message,
                source = logEntry.source,
                metadata = logEntry.metadata,
                tags = logEntry.tags
            )
            return logRepository.save(updated)
        } catch (e: OptimisticLockingFailureException) {
            throw IllegalStateException("Log was modified by another process. Please retry.", e)
        }
    }
    
    fun deleteLog(id: String) {
        logRepository.deleteById(id)
    }
    
    private suspend fun checkAndCreateAlerts(logEntry: LogEntry) {
        if (logEntry.level == LogLevel.ERROR || logEntry.level == LogLevel.FATAL) {
            alertService.checkErrorRateAndCreateAlert(logEntry.source)
        }
    }
}
