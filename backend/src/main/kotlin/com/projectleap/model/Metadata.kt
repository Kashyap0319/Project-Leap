package com.projectleap.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "metadata")
data class SystemMetadata(
    @Id
    val id: String? = null,
    
    val key: String,
    
    val value: String,
    
    val category: String,
    
    val description: String? = null,
    
    val lastUpdated: Instant = Instant.now(),
    
    @Version
    val version: Long? = null
)

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    
    val username: String,
    
    val password: String,
    
    val email: String,
    
    val roles: Set<String> = setOf("USER"),
    
    val enabled: Boolean = true,
    
    val createdAt: Instant = Instant.now(),
    
    @Version
    val version: Long? = null
)

@Document(collection = "rate_limit_stats")
data class RateLimitStat(
    @Id
    val id: String? = null,
    
    val userId: String,
    
    val endpoint: String,
    
    val timestamp: Instant = Instant.now(),
    
    val requestCount: Long,
    
    val blocked: Boolean = false
)
