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
                    requestBytes = it.requestBytes,
                    responseBytes = it.responseBytes
                )
            )
        }
        bulk.execute()
        alertService.evaluateAndPersist(events)
    }

    fun query(
        service: String?,
        endpoint: String?,
        status: Int?,
        slow: Boolean?,
        broken: Boolean?,
        rateLimited: Boolean?,
        startTs: Long?,
        endTs: Long?
    ): List<LogDocument> {
        val criteria = mutableListOf<Criteria>()
        service?.let { criteria += Criteria.where("service").`is`(it) }
        endpoint?.let { criteria += Criteria.where("endpoint").`is`(it) }
        status?.let { criteria += Criteria.where("status").`is`(it) }
        if (slow == true) criteria += Criteria.where("latencyMs").gt(500)
        if (broken == true) criteria += Criteria.where("status").gte(500)
        rateLimited?.let { criteria += Criteria.where("rateLimited").`is`(it) }
        if (startTs != null) criteria += Criteria.where("timestamp").gte(startTs)
        if (endTs != null) criteria += Criteria.where("timestamp").lte(endTs)

        val query = if (criteria.isEmpty()) Query() else Query(Criteria().andOperator(*criteria.toTypedArray()))
        query.limit(500)
        return logsTemplate.find(query, LogDocument::class.java)
    }
}
