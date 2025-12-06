package com.projectleap.collector.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.jwt")
data class SecurityProps(
    var secret: String = "change-me-super-secret-256-bit",
    var issuer: String = "api-monitoring"
)
