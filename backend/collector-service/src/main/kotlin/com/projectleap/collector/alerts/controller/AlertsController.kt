package com.projectleap.collector.alerts.controller

import com.projectleap.collector.alerts.service.AlertService
import com.projectleap.collector.dto.AlertRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import jakarta.validation.Valid

@RestController
@RequestMapping("/api/alerts")
class AlertsController(
    private val alertService: AlertService
) {

    @GetMapping
    fun getAlerts() = ResponseEntity.ok(alertService.getActiveAlerts())

    @PostMapping("/{id}/resolve")
    fun resolve(@PathVariable id: String) =
        ResponseEntity.ok(alertService.resolveAlert(id))

    @PostMapping
    fun createAlert(@Valid @RequestBody request: AlertRequest): ResponseEntity<Any> {
        val alert = alertService.createAlert(
            message = request.message,
            service = request.service,
            endpoint = request.endpoint,
            type = request.type,
            severity = request.severity
        )
        return ResponseEntity.ok(alert)
    }
}
