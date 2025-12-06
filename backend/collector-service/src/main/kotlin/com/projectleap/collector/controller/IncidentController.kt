package com.projectleap.collector.controller

import com.projectleap.collector.service.IncidentService
import com.projectleap.contracts.Incident
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/incidents")
class IncidentController(
    private val incidentService: IncidentService
) {
    data class ResolveRequest(val version: Long)

    @GetMapping
    fun listOpen(): List<Incident> = incidentService.listOpen()

    @PostMapping("/{id}/resolve")
    fun resolve(@PathVariable id: String, @RequestBody req: ResolveRequest): ResponseEntity<Incident> =
        ResponseEntity.ok(incidentService.resolve(id, req.version))
}
