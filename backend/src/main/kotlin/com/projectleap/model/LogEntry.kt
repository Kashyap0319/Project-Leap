package com.projectleap.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "logs")
data class LogEntry(
    @Id
    val id: String? = null,
    
    val timestamp: Instant = Instant.now(),
    
    val level: LogLevel,
    
    val message: String,
    
    val source: String,
    
    val metadata: Map<String, Any> = emptyMap(),
    
    val tags: List<String> = emptyList(),
    
    val userId: String? = null,
    
    @Version
    val version: Long? = null
)

enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
}
