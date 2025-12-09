package com.projectleap.tracker

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(MonitoringProperties::class)
@ConfigurationProperties(prefix = "monitoring")
data class MonitoringProperties(
    var serviceName: String = "unknown-service",
    var collectorUrl: String = "http://localhost:8080",
    var jwtSecret: String = "7fbdc81f7e5a46b8a9f5258a9a6ec8da58be0e337a0cc8f5c19a43512e8c7f26",
    var rateLimit: RateLimit = RateLimit()
) {
    data class RateLimit(
        var limit: Long = 100,
        var burst: Long = 100
    )
}
