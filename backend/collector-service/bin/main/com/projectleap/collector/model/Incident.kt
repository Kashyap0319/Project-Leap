package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "incidents")
data class Incident(
    @Id val id: String? = null,
    val alertId: String,
    val service: String? = null,
    val endpoint: String? = null,
    val type: String? = null,
    val severity: String? = null,
    val message: String? = null,
    val firstSeen: Instant = Instant.now(),
    val lastSeen: Instant = Instant.now(),
    val occurrences: Int = 1,
    val resolved: Boolean = false,
    @Version val version: Long? = null
)
