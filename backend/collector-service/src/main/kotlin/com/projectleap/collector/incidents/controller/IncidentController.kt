package com.projectleap.collector.incidents.controller

import com.projectleap.collector.incidents.service.IncidentService
import com.projectleap.collector.dto.IncidentRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import jakarta.validation.Valid

@RestController
@RequestMapping("/api/incidents")
class IncidentController(
    private val incidentService: IncidentService
) {

    @GetMapping
    fun getIncidents() = ResponseEntity.ok(incidentService.getAllIncidents())

    @PostMapping("/{id}/resolve")
    fun resolve(
        @PathVariable id: String,
        @RequestBody body: Map<String, Long>
    ): ResponseEntity<Map<String, Any>> {
        val version = body["version"]
            ?: throw IllegalArgumentException("Version parameter is required")
        
        return try {
            val resolved = incidentService.resolveIncident(id, version)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "incident" to resolved
            ))
        } catch (e: org.springframework.dao.OptimisticLockingFailureException) {
            ResponseEntity.status(409).body(mapOf(
                "success" to false,
                "error" to "Version conflict. Please refresh and try again.",
                "message" to (e.message ?: "Optimistic locking failure")
            ))
        }
    }

    @PostMapping
    fun createIncident(@Valid @RequestBody request: IncidentRequest): ResponseEntity<Any> {
        val incident = incidentService.createIncident(request.message)
        return ResponseEntity.ok(incident)
    }
}
