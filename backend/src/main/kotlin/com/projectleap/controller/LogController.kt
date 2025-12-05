package com.projectleap.controller

import com.projectleap.model.LogEntry
import com.projectleap.model.LogLevel
import com.projectleap.service.LogService
import com.projectleap.util.RateLimiter
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.Instant

data class LogRequest(
    val level: LogLevel,
    val message: String,
    val source: String,
    val metadata: Map<String, Any> = emptyMap(),
    val tags: List<String> = emptyList()
)

data class BulkLogRequest(
    val logs: List<LogRequest>
)

@RestController
@RequestMapping("/api/logs")
class LogController(
    private val logService: LogService,
    private val rateLimiter: RateLimiter
) {
    
    @PostMapping
    fun createLog(
        @RequestBody logRequest: LogRequest,
        authentication: Authentication,
        request: HttpServletRequest
    ): ResponseEntity<LogEntry> = runBlocking {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        val logEntry = LogEntry(
            level = logRequest.level,
            message = logRequest.message,
            source = logRequest.source,
            metadata = logRequest.metadata,
            tags = logRequest.tags,
            userId = authentication.name
        )
        
        val saved = logService.createLog(logEntry)
        ResponseEntity.ok(saved)
    }
    
    @PostMapping("/bulk")
    fun createLogsBulk(
        @RequestBody bulkRequest: BulkLogRequest,
        authentication: Authentication,
        request: HttpServletRequest
    ): ResponseEntity<List<LogEntry>> = runBlocking {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        val logEntries = bulkRequest.logs.map { logRequest ->
            LogEntry(
                level = logRequest.level,
                message = logRequest.message,
                source = logRequest.source,
                metadata = logRequest.metadata,
                tags = logRequest.tags,
                userId = authentication.name
            )
        }
        
        val saved = logService.createLogsBulk(logEntries)
        ResponseEntity.ok(saved)
    }
    
    @GetMapping
    fun getLogs(
        @RequestParam(required = false) level: LogLevel?,
        @RequestParam(required = false) source: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: Instant?,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<List<LogEntry>> {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        val logs = logService.getLogs(level, source, startTime, endTime)
        return ResponseEntity.ok(logs)
    }
    
    @GetMapping("/{id}")
    fun getLog(
        @PathVariable id: String,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<LogEntry> {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        val log = logService.getLogById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(log)
    }
    
    @PutMapping("/{id}")
    fun updateLog(
        @PathVariable id: String,
        @RequestBody logRequest: LogRequest,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<LogEntry> {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        val logEntry = LogEntry(
            level = logRequest.level,
            message = logRequest.message,
            source = logRequest.source,
            metadata = logRequest.metadata,
            tags = logRequest.tags,
            userId = authentication.name
        )
        
        val updated = logService.updateLog(id, logEntry)
        return ResponseEntity.ok(updated)
    }
    
    @DeleteMapping("/{id}")
    fun deleteLog(
        @PathVariable id: String,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<Void> {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        logService.deleteLog(id)
        return ResponseEntity.noContent().build()
    }
}
