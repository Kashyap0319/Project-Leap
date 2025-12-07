package com.projectleap.collector.controller

import com.projectleap.collector.service.ServiceAnalyticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/services")
class ServiceAnalyticsController(
    private val analyticsService: ServiceAnalyticsService
) {
    @GetMapping
    fun list(
        @RequestParam(required = false, defaultValue = "24h") window: String
    ): List<ServiceAnalyticsService.ServiceSummary> {
        val windowMs = when (window) {
            "1h" -> 60 * 60 * 1000L
            "7d" -> 7L * 24 * 60 * 60 * 1000
            else -> 24 * 60 * 60 * 1000L
        }
        return analyticsService.summarize(windowMs = windowMs)
    }
}
