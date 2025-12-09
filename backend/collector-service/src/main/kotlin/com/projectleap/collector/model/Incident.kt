package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "incidents")
data class Incident(
    @Id val id: String? = null,
    val alertId: String,
    val createdAt: Instant = Instant.now(),
    val resolved: Boolean = false,
    @Version val version: Long? = null
)
