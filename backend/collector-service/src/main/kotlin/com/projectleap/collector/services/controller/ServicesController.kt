package com.projectleap.collector.services.controller

import com.projectleap.collector.logs.repository.LogEntryRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/services")
class ServicesController(
    private val logEntryRepository: LogEntryRepository
) {
    @GetMapping
    fun getServices(): ResponseEntity<List<Map<String, Any>>> {
        val logs = logEntryRepository.findAll()
        // Filter out entries with null service (they're invalid)
        val validLogs = logs.filter { it.service != null && !it.service.isBlank() }
        val grouped = validLogs.groupBy { it.service ?: "unknown" }
        val summaries = grouped.map { (service, entries) ->
            val validEntries = entries.filter { 
                it.latencyMs != null && 
                it.statusCode != null && 
                it.timestamp != null
            }
            val count = validEntries.size
            val avgLatency = if (count > 0) validEntries.mapNotNull { it.latencyMs }.average() else 0.0
            val errorRate = if (count > 0) validEntries.count { (it.statusCode ?: 0) >= 400 }.toDouble() / count else 0.0
            
            // Calculate endpoint stats
            val endpointStats = validEntries.groupBy { "${it.method} ${it.endpoint}" }
                .map { (endpoint, endpointLogs) ->
                    val endpointLatencies = endpointLogs.mapNotNull { it.latencyMs }
                    val endpointErrors = endpointLogs.count { (it.statusCode ?: 0) >= 400 }
                    mapOf<String, Any>(
                        "path" to endpoint,
                        "method" to (endpointLogs.firstOrNull()?.method ?: "GET"),
                        "avgLatency" to if (endpointLatencies.isNotEmpty()) endpointLatencies.average() else 0.0,
                        "p95Latency" to calculateP95(endpointLatencies),
                        "errorRate" to if (endpointLogs.isNotEmpty()) endpointErrors.toDouble() / endpointLogs.size else 0.0,
                        "requestCount" to endpointLogs.size
                    )
                }
                .sortedByDescending { it["avgLatency"] as Double }
                .take(5)
            
            // Generate latency trend (last 20 data points, sorted by timestamp)
            val latencyTrend = validEntries
                .filter { it.timestamp != null && it.latencyMs != null }
                .sortedBy { it.timestamp } // Sort by timestamp ascending
                .takeLast(20) // Take last 20
                .map { log ->
                    mapOf(
                        "timestamp" to (log.timestamp?.toString() ?: ""),
                        "latencyMs" to (log.latencyMs ?: 0L)
                    )
                }
            
            mapOf(
                "name" to (service ?: "unknown"),
                "requests" to count,
                "avgLatency" to avgLatency,
                "errorRate" to errorRate,
                "latencyTrend" to latencyTrend,
                "endpoints" to endpointStats
            )
        }
        return ResponseEntity.ok(summaries)
    }
    
    @GetMapping("/widgets")
    fun getWidgets(@RequestParam(required = false) window: String?): ResponseEntity<Map<String, Any>> {
        val logs = logEntryRepository.findAll()
        val now = Instant.now()
        val windowStart = when (window) {
            "1h" -> now.minusSeconds(3600)
            "24h" -> now.minusSeconds(86400)
            "7d" -> now.minusSeconds(604800)
            else -> now.minusSeconds(86400) // Default 24h
        }
        
        val filteredLogs = logs.filter { 
            it.timestamp != null && it.timestamp!!.isAfter(windowStart)
        }
        
        val slowCount = filteredLogs.count { (it.latencyMs ?: 0) > 500 }
        val brokenCount = filteredLogs.count { (it.statusCode ?: 0) >= 500 }
        val rateLimitViolations = filteredLogs.count { it.rateLimited == true }
        
        // Top 5 slow endpoints
        val slowEndpoints = filteredLogs
            .filter { (it.latencyMs ?: 0) > 500 }
            .groupBy { "${it.method} ${it.endpoint}" }
            .map { (endpoint, endpointLogs) ->
                val avgLatency = endpointLogs.mapNotNull { it.latencyMs }.average()
                mapOf(
                    "endpoint" to endpoint,
                    "avgLatency" to avgLatency,
                    "count" to endpointLogs.size
                )
            }
            .sortedByDescending { it["avgLatency"] as Double }
            .take(5)
        
        // Error rate graph data (hourly buckets)
        val errorRateData = generateErrorRateData(filteredLogs, windowStart, now)
        
        return ResponseEntity.ok(mapOf(
            "slowApiCount" to slowCount,
            "brokenApiCount" to brokenCount,
            "rateLimitViolations" to rateLimitViolations,
            "top5SlowEndpoints" to slowEndpoints,
            "errorRateGraph" to errorRateData
        ))
    }
    
    private fun calculateP95(latencies: List<Long>): Double {
        if (latencies.isEmpty()) return 0.0
        val sorted = latencies.sorted()
        val index = (sorted.size * 0.95).toInt()
        return sorted.getOrElse(index) { sorted.last() }.toDouble()
    }
    
    private fun generateErrorRateData(logs: List<com.projectleap.collector.model.LogEntry>, start: Instant, end: Instant): List<Map<String, Any>> {
        val buckets = mutableMapOf<String, Pair<Int, Int>>() // timestamp -> (errors, total)
        
        logs.forEach { log ->
            val hour = log.timestamp?.toString()?.substring(0, 13) ?: return@forEach
            val current = buckets.getOrDefault(hour, Pair(0, 0))
            val isError = (log.statusCode ?: 0) >= 400
            buckets[hour] = Pair(
                current.first + if (isError) 1 else 0,
                current.second + 1
            )
        }
        
        return buckets.map { (timestamp, counts) ->
            mapOf(
                "timestamp" to timestamp,
                "errorRate" to if (counts.second > 0) counts.first * 100.0 / counts.second else 0.0,
                "totalRequests" to counts.second,
                "errors" to counts.first
            )
        }.sortedBy { it["timestamp"] as String }
    }
}
