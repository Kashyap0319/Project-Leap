package com.projectleap.collector.service

import com.projectleap.collector.model.LogDocument
import com.projectleap.contracts.LogEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class LogIngestService(
    @Qualifier("logsTemplate") private val logsTemplate: MongoTemplate,
    private val alertService: AlertService
) {

    fun saveBatch(events: List<LogEvent>) {
        if (events.isEmpty()) return
        val bulk = logsTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, LogDocument::class.java)
        events.forEach {
            bulk.insert(
                LogDocument(
                    service = it.service,
                    endpoint = it.endpoint,
                    method = it.method,
                    status = it.status,
                    latencyMs = it.latencyMs,
                    rateLimited = it.rateLimited,
                    timestamp = it.timestamp,
                    requestId = it.requestId,
                    requestSizeBytes = it.requestBytes,
                    responseSizeBytes = it.responseBytes
                )
            )
        }
        bulk.execute()
        alertService.evaluateAndPersist(events)
    }

    fun query(
        service: String?,
        endpoint: String?,
        status: String?,
        slow: Boolean?,
        broken: Boolean?,
        rateLimited: Boolean?,
        errorsOnly: Boolean?,
        startTs: Long?,
        endTs: Long?,
        q: String?,
        window: String?,
        page: Int,
        size: Int
    ): List<LogDocument> {
        val criteria = mutableListOf<Criteria>()
        service?.let { criteria += Criteria.where("service").`is`(it) }
        endpoint?.let { criteria += Criteria.where("endpoint").`is`(it) }
        status?.let { statusCriteria(criteria, it) }
        if (slow == true) criteria += Criteria.where("latencyMs").gt(500)
        if (broken == true) criteria += Criteria.where("status").gte(500)
        rateLimited?.let { criteria += Criteria.where("rateLimited").`is`(it) }
        if (errorsOnly == true) criteria += Criteria.where("status").gte(400)
        q?.takeIf { it.isNotBlank() }?.let {
            criteria += Criteria.where("endpoint").regex(it, "i")
        }
        window?.let { win ->
            val now = System.currentTimeMillis()
            val lookback = when (win) {
                "1h" -> now - 60 * 60 * 1000
                "24h" -> now - 24 * 60 * 60 * 1000
                "7d" -> now - 7L * 24 * 60 * 60 * 1000
                else -> null
            }
            if (lookback != null) criteria += Criteria.where("timestamp").gte(lookback)
        }
        if (startTs != null) criteria += Criteria.where("timestamp").gte(startTs)
        if (endTs != null) criteria += Criteria.where("timestamp").lte(endTs)

        val query = if (criteria.isEmpty()) Query() else Query(Criteria().andOperator(*criteria.toTypedArray()))
        val cappedSize = size.coerceIn(1, 500)
        val skip = (page.coerceAtLeast(0)) * cappedSize
        query.skip(skip.toLong())
        query.limit(cappedSize)
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"))
        return logsTemplate.find(query, LogDocument::class.java)
    }

    private fun statusCriteria(criteria: MutableList<Criteria>, status: String) {
        when (status.lowercase()) {
            "2xx" -> criteria += Criteria.where("status").gte(200).lt(300)
            "4xx" -> criteria += Criteria.where("status").gte(400).lt(500)
            "5xx" -> criteria += Criteria.where("status").gte(500)
            "429" -> criteria += Criteria.where("status").`is`(429)
            else -> status.toIntOrNull()?.let { criteria += Criteria.where("status").`is`(it) }
        }
    }
}
