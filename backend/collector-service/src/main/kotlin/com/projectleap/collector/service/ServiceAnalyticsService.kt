package com.projectleap.collector.service

import com.projectleap.collector.model.LogDocument
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.domain.Sort
import kotlin.math.ceil

@Service
class ServiceAnalyticsService(
    @Qualifier("logsTemplate") private val logsTemplate: MongoTemplate
) {
    data class EndpointStats(
        val path: String,
        val method: String,
        val avgLatency: Double,
        val p95Latency: Double?,
        val errorRate: Double,
        val requestCount: Long
    )

    data class ServiceSummary(
        val name: String,
        val requests: Long,
        val avgLatency: Double,
        val errorRate: Double,
        val latencyTrend: List<LatencyPoint>,
        val endpoints: List<EndpointStats>
    )

    data class LatencyPoint(val timestamp: Long, val latencyMs: Long)

    fun summarize(windowMs: Long = 24 * 60 * 60 * 1000, limit: Int = 5000): List<ServiceSummary> {
        val now = System.currentTimeMillis()
        val query = Query(Criteria.where("timestamp").gte(now - windowMs))
        query.limit(limit)
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"))
        val logs = logsTemplate.find(query, LogDocument::class.java)
        if (logs.isEmpty()) return emptyList()

        val byService = logs.groupBy { it.service }
        return byService.map { (service, entries) ->
            val avgLatency = entries.map { it.latencyMs }.average()
            val errors = entries.count { it.status >= 500 }
            val errorRate = if (entries.isNotEmpty()) errors.toDouble() / entries.size else 0.0
            val trend = entries.take(200).map { LatencyPoint(it.timestamp, it.latencyMs) }.sortedBy { it.timestamp }

            val endpoints = entries
                .groupBy { Pair(it.endpoint, it.method) }
                .map { (key, items) ->
                    val latencies = items.map { it.latencyMs }.sorted()
                    val p95 = percentile(latencies, 0.95)
                    EndpointStats(
                        path = key.first,
                        method = key.second,
                        avgLatency = items.map { it.latencyMs }.average(),
                        p95Latency = p95,
                        errorRate = if (items.isNotEmpty()) items.count { it.status >= 500 }.toDouble() / items.size else 0.0,
                        requestCount = items.size.toLong()
                    )
                }
                .sortedByDescending { it.requestCount }

            ServiceSummary(
                name = service,
                requests = entries.size.toLong(),
                avgLatency = avgLatency,
                errorRate = errorRate,
                latencyTrend = trend,
                endpoints = endpoints
            )
        }
    }

    private fun percentile(sorted: List<Long>, quantile: Double): Double? {
        if (sorted.isEmpty()) return null
        val idx = ceil(quantile * sorted.size).toInt() - 1
        return sorted[idx.coerceIn(0, sorted.size - 1)].toDouble()
    }
}
