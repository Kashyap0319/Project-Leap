package com.projectleap.collector.incidents.controller

import com.projectleap.collector.incidents.service.IncidentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/incidents")
class IncidentController(
    private val incidentService: IncidentService
) {

    @GetMapping
    fun getIncidents() = ResponseEntity.ok(incidentService.getAllIncidents())

    @PostMapping("/{id}/resolve")
    fun resolve(@PathVariable id: String) =
        ResponseEntity.ok(incidentService.resolveIncident(id))
}
