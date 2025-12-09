package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "alerts")
data class Alert(
    @Id val id: String? = null,
    val message: String,
    val createdAt: Instant = Instant.now(),
    val resolved: Boolean = false
)
