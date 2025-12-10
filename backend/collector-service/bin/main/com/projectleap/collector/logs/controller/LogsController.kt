package com.projectleap.collector.logs.controller

import com.projectleap.collector.dto.LogRequest
import com.projectleap.collector.logs.service.LogService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/logs")
class LogsController(
    private val logService: LogService
) {

    @PostMapping
    fun saveLog(@Valid @RequestBody request: LogRequest) =
        ResponseEntity.ok(logService.saveLog(request))

    @PostMapping("/batch")
    fun saveLogsBatch(@Valid @RequestBody requests: List<LogRequest>) =
        ResponseEntity.ok(requests.map { logService.saveLog(it) })

    @GetMapping
    fun getLogs(
        @RequestParam(required = false) service: String?,
        @RequestParam(required = false) endpoint: String?,
        @RequestParam(required = false) status: Int?,
        @RequestParam(required = false) from: Instant?,
        @RequestParam(required = false) to: Instant?,
        @RequestParam(required = false) slow: Boolean?,
        @RequestParam(required = false) broken: Boolean?,
        @RequestParam(required = false) rateLimited: Boolean?,
        @RequestParam(required = false) errorsOnly: Boolean?,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) window: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val pageable = PageRequest.of(page, size)
        val logs = logService.getLogs(service, from, to, pageable)
        
        // Apply additional filters
        var filtered = logs.content.toList()
        
        if (endpoint != null) {
            filtered = filtered.filter { it.endpoint?.contains(endpoint, ignoreCase = true) == true }
        }
        
        if (status != null) {
            filtered = filtered.filter { it.statusCode == status }
        }
        
        if (slow == true) {
            filtered = filtered.filter { (it.latencyMs ?: 0) > 500 }
        }
        
        if (broken == true) {
            filtered = filtered.filter { (it.statusCode ?: 0) >= 500 }
        }
        
        if (rateLimited == true) {
            filtered = filtered.filter { (it.rateLimited ?: false) == true }
        }
        
        if (errorsOnly == true) {
            filtered = filtered.filter { (it.statusCode ?: 0) >= 400 }
        }
        
        if (q != null) {
            val query = q.lowercase()
            filtered = filtered.filter {
                it.service?.lowercase()?.contains(query) == true ||
                it.endpoint?.lowercase()?.contains(query) == true ||
                it.method?.lowercase()?.contains(query) == true
            }
        }
        
        // Apply window filter (1h, 24h, 7d)
        if (window != null) {
            val now = Instant.now()
            val windowStart = when (window) {
                "1h" -> now.minusSeconds(3600)
                "24h" -> now.minusSeconds(86400)
                "7d" -> now.minusSeconds(604800)
                else -> null
            }
            if (windowStart != null) {
                filtered = filtered.filter { 
                    it.timestamp != null && it.timestamp!!.isAfter(windowStart)
                }
            }
        }
        
        return ResponseEntity.ok(mapOf(
            "content" to filtered,
            "totalElements" to filtered.size,
            "totalPages" to 1,
            "page" to page,
            "size" to size
        ))
    }
}
