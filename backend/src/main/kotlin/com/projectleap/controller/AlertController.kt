package com.projectleap.controller

import com.projectleap.model.Alert
import com.projectleap.model.AlertStatus
import com.projectleap.service.AlertService
import com.projectleap.util.RateLimiter
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

data class ResolveAlertRequest(
    val resolution: String
)

@RestController
@RequestMapping("/api/alerts")
class AlertController(
    private val alertService: AlertService,
    private val rateLimiter: RateLimiter
) {
    
    @GetMapping
    fun getAllAlerts(
        @RequestParam(required = false) status: AlertStatus?,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<List<Alert>> {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        val alerts = if (status != null) {
            alertService.getAlertsByStatus(status)
        } else {
            alertService.getAllAlerts()
        }
        return ResponseEntity.ok(alerts)
    }
    
    @GetMapping("/{id}")
    fun getAlert(
        @PathVariable id: String,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<Alert> {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        val alert = alertService.getAlertById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(alert)
    }
    
    @PostMapping("/{id}/resolve")
    fun resolveAlert(
        @PathVariable id: String,
        @RequestBody resolveRequest: ResolveAlertRequest,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<Alert> {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        val resolved = alertService.resolveAlert(id, authentication.name, resolveRequest.resolution)
        return ResponseEntity.ok(resolved)
    }
    
    @PostMapping("/{id}/dismiss")
    fun dismissAlert(
        @PathVariable id: String,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<Alert> {
        rateLimiter.checkRateLimit(request, authentication.name)
        
        val dismissed = alertService.dismissAlert(id)
        return ResponseEntity.ok(dismissed)
    }
}
