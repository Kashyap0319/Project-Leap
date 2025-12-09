package com.projectleap.collector.incidents.service

import com.projectleap.collector.incidents.repository.IncidentRepository
import com.projectleap.collector.model.Incident
import org.springframework.stereotype.Service

@Service
class IncidentService(
    private val incidentRepository: IncidentRepository
) {

    fun createIncident(alertId: String): Incident =
        incidentRepository.save(Incident(alertId = alertId))

    fun resolveIncident(id: String): Incident {
        val incident = incidentRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Incident not found") }
        val updated = incident.copy(resolved = true)
        return incidentRepository.save(updated)
    }

    fun getAllIncidents(): List<Incident> =
        incidentRepository.findAll()
}
