package com.projectleap.collector.alerts.controller

import com.projectleap.collector.alerts.service.AlertService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
}
