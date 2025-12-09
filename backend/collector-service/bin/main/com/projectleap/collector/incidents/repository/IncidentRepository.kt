package com.projectleap.collector.incidents.repository

import com.projectleap.collector.model.Incident
import org.springframework.data.mongodb.repository.MongoRepository

interface IncidentRepository : MongoRepository<Incident, String> {
    fun findByResolved(resolved: Boolean): List<Incident>
}
