package com.projectleap.collector.service

import com.projectleap.collector.model.AlertDocument
import com.projectleap.collector.model.IncidentDocument
import com.projectleap.contracts.Incident
import com.projectleap.contracts.IncidentStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class IncidentService(
    @Qualifier("metaTemplate") private val metaTemplate: MongoTemplate
) {

    @Transactional("metaTxManager")
    fun touchIncident(alert: AlertDocument) {
        val query = Query(
            Criteria.where("service").`is`(alert.service)
                .and("endpoint").`is`(alert.endpoint)
                .and("type").`is`(alert.type)
                .and("status").`is`(IncidentStatus.OPEN)
        )
        val update = Update()
            .setOnInsert("firstSeen", alert.triggeredAt)
            .setOnInsert("service", alert.service)
            .setOnInsert("endpoint", alert.endpoint)
            .setOnInsert("type", alert.type)
            .setOnInsert("severity", alert.severity)
            .setOnInsert("version", 0L)
            .inc("occurrences", 1)
            .set("lastSeen", alert.triggeredAt)
        metaTemplate.findAndModify(query, update, FindAndModifyOptions.options().upsert(true).returnNew(true), IncidentDocument::class.java)
    }

    @Transactional("metaTxManager")
    fun resolve(id: String, expectedVersion: Long): Incident {
        val query = Query(
            Criteria.where("_id").`is`(id)
                .and("version").`is`(expectedVersion)
                .and("status").`is`(IncidentStatus.OPEN)
        )
        val update = Update()
            .set("status", IncidentStatus.RESOLVED)
            .set("resolvedAt", Instant.now().toEpochMilli())
            .inc("version", 1)
        val updated = metaTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), IncidentDocument::class.java)
            ?: throw OptimisticLockingFailureException("Version mismatch for incident $id")
        return updated.toDto()
    }

    fun listOpen(): List<Incident> =
        metaTemplate.find(Query(Criteria.where("status").`is`(IncidentStatus.OPEN)), IncidentDocument::class.java)
            .map { it.toDto() }

    private fun IncidentDocument.toDto() = Incident(
        id = id,
        service = service,
        endpoint = endpoint,
        type = type,
        status = status,
        firstSeen = firstSeen,
        lastSeen = lastSeen,
        occurrences = occurrences,
        severity = severity,
        version = version
    )
}
