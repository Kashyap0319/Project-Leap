package com.projectleap.collector.incidents.service

import com.projectleap.collector.incidents.repository.IncidentRepository
import com.projectleap.collector.model.Incident
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class IncidentService(
    private val incidentRepository: IncidentRepository
) {

    fun createIncident(alertId: String, service: String? = null, endpoint: String? = null, severity: String? = null, message: String? = null): Incident =
        incidentRepository.save(
            Incident(
                alertId = alertId,
                service = service,
                endpoint = endpoint,
                severity = severity,
                message = message
            )
        )

    @Transactional
    fun resolveIncident(id: String, version: Long): Incident {
        val incident = incidentRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Incident not found: $id") }
        
        if (incident.version != version) {
            throw OptimisticLockingFailureException(
                "Incident version mismatch. Expected: $version, Actual: ${incident.version}. " +
                "Another process may have updated this incident."
            )
        }
        
        val updated = incident.copy(resolved = true)
        return incidentRepository.save(updated)
    }

    fun getAllIncidents(): List<Incident> =
        incidentRepository.findAll()
}
